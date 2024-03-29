/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2015                                             
 *                                                                              
 *  Creation Date: 20.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.security;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * class for decoding an obfuscated/scrambled string
 */
public final class StringScrambler {
    /**
     * The opposite to this method, obfuscate, is found in
     * org.oscm.tools.StringScrambler.
     * 
     * @param obfuscated
     * @return original string
     */
    // For security reasons the encode part will not be shipped to the customer.
    public static final String decode(final long[] obfuscated) {
        final int length = obfuscated.length;
        final byte[] encoded = new byte[8 * (length - 1)];
        final long seed = obfuscated[0];
        final Random prng = new Random(seed);

        for (int i = 1; i < length; i++) {
            final long key = prng.nextLong();
            final int off = 8 * (i - 1);
            long l = obfuscated[i] ^ key;
            final int end = Math.min(encoded.length, off + 8);
            for (int i2 = off; i2 < end; i2++) {
                encoded[i2] = (byte) l;
                l >>= 8;
            }
        }

        final String decoded;
        try {
            decoded = new String(encoded, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }

        final int i = decoded.indexOf(0);
        return i != -1 ? decoded.substring(0, i) : decoded;
    }
}
