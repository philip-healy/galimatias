/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias.canonicalize;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

import static io.mola.galimatias.CodePoints.*;

public class RFC2396Canonicalizer extends BaseURLCanonicalizer {

    @Override
    public URL canonicalize(URL url) throws GalimatiasParseException {
        // User
        if (url.username() != null && !url.username().isEmpty()) {
            url = url.withUsername(canonicalize(url.username(), USERINFO_PREDICATE));
        }

        // Pass
        if (url.password() != null && !url.password().isEmpty()) {
            url = url.withPassword(canonicalize(url.password(), USERINFO_PREDICATE));
        }

        // Path
        if (url.path() != null) {
            url = url.withPath(canonicalize(url.path(), PATH_PREDICATE));
        }

        final String query = url.query();
        if (query != null && query.length() > 1) {
            url = url.withQuery(canonicalize(query.substring(1), URIC_PREDICATE));
        }

        final String fragment = url.fragment();
        if (fragment != null && fragment.length() > 1) {
            url = url.withFragment(canonicalize(fragment.substring(1), URIC_PREDICATE));
        }

        return url;
    }

    private static boolean isMark(final int c) {
        return c == '-' || c  == '_' || c == '.' || c == '!' || c == '*' || c == '\'' || c ==  '(' || c == ')';
    }

    private static boolean isUnreserved(final int c) {
        return isASCIIAlphanumeric(c) || isMark(c);
    }

    private static boolean isReserved(final int c) {
        return c == ';' || c == '/' || c == '?' || c == ':' || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    private static boolean isPChar(final int c) {
        //XXX: "pct-encoded" is pchar, but we check for it before calling this.
        return isUnreserved(c) || c == ':' || c == '@' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    private static boolean isUric(final int c) {
        return isReserved(c) || isUnreserved(c);
    }

    private static boolean isUserInfo(final int c) {
        //XXX: ':' is excluded here, since we work with user/pass, not userInfo
        return isUnreserved(c) || c == ';' || c == ':' || c == '&' || c == '=' || c == '+' || c == '$' || c == ',';
    }

    private static final CharacterPredicate URIC_PREDICATE = new CharacterPredicate() {
        @Override
        public boolean test(int c) {
          return isUric(c);
        }
    };

    private static final CharacterPredicate PATH_PREDICATE = new CharacterPredicate() {
        @Override
        public boolean test(int c) {
            return isPChar(c) || c == '/';
        }
    };

    private static final CharacterPredicate USERINFO_PREDICATE = new CharacterPredicate() {
        @Override
        public boolean test(int c) {
            return isUserInfo(c);
        }
    };

}
