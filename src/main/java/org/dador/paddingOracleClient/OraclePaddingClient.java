package org.dador.paddingOracleClient;


import java.io.IOException;
import java.net.URISyntaxException;

import static org.dador.paddingOracleClient.HexConverters.toByteArrayFromHex;
import static org.dador.paddingOracleClient.HexConverters.toHexFromByteArray;
import static org.dador.paddingOracleClient.HexConverters.toPrintableString;

/**
 * Main Class for Padding OracleClient
 */
public class OraclePaddingClient {
    static final String ENCRYPTED_MESSAGE = "5ca00ff4c878d61e1edbf1700618fb287c21578c0580965dad57f70636ea402fa0017c4acc82717730565174e2e3f713d3921bab07cba15f3197b87976525ce4";
    static final int BLOCK_SIZE = 16;

    /**
     * Fonction takes a number and creates a block of x00 values, padded according to PKCS#7
     * example : n=3 result is 00 00 .. 00 03 03 03
     * @param n : number of bytes of padding
     * @return byte[BLOCK_SIZE] filled with 0 and padding values
     */
    protected byte[] buildPaddingArray(int n) {
        byte[] result = new byte[BLOCK_SIZE];

        /**
         * TODO : Your CODE HERE
         */
        return result;
    }

    /**
     * Function that create a modified ciphertext bloc for trying a guess
     * Note that the "ciphertext" correspond to the IV part for the Block Cipher
     * @param ciphertext : original ciphertext bloc
     * @param decoded    : decrypted part of the plain text (for next bloc)
     * @param position   : position of the byte to guess
     * @param guess      : the guess for this query
     * @return a byte array with c0...c(i-1)||ci+i+g||cj+mj+i||...||cn+mn+i
     */
    protected byte[] buildGuessForPosition(byte[] ciphertext, byte[] decoded, int position, byte guess) {
        byte[] result = new byte[BLOCK_SIZE];

        /**
         * TODO : YOUR CODE HERE
         */

        return result;
    }

    /**
     * Fonction that splits a message into constituent blocs of BLOCK_SIZE
     *
     * @param message
     * @return an array of blocs
     * @throws IllegalArgumentException
     */
    protected byte[][] splitMessageIntoBlocks(byte[] message) throws IllegalArgumentException {
        if (message.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("Message length is not a multiple of bloc size");
        }

        int blocNumber = message.length / BLOCK_SIZE;

        byte[][] result = new byte[blocNumber][BLOCK_SIZE];

        for (int i = 0; i < blocNumber; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                result[i][j] = message[i * BLOCK_SIZE + j];
            }
        }
        return result;
    }

    /**
     * Function that takes the 2 last blocks of the message
     * and returns the length of the padding.
     * @param poq : a PaddingOracleQuery object
     * @param previousbloc : next to last block of the ciphertext
     * @param lastbloc : last bloc of the ciphertext
     * @return an integer corresponding to padding length
     * @throws IOException
     * @throws URISyntaxException
     */
    public int getPaddingLengthForLastBlock(PaddingOracleQuery poq, byte[] previousbloc, byte[] lastbloc) throws IOException, URISyntaxException {
        /**
         * TODO : Your Code HERE
         */
        // should not arrive here !
        return 0;
    }

    /**
     * Main function that takes 2 consecutive blocks of the ciphertext
     * and returns the decryption of the 2nd message block
     *
     * @param poq : a PaddingOracleQuery object to query server
     * @param iv : the "iv" part of the 2 blocks query
     * @param ciphertext : the block that will be decrypted
     * @param padding : set to 0 if not the last block. Set to paddinglength if last block
     * @return a decrypted byte array
     * @throws IOException
     * @throws URISyntaxException
     */
    public byte[] runDecryptionForBlock(PaddingOracleQuery poq, byte[] iv, byte[] ciphertext, int padding) throws IOException, URISyntaxException {
        byte[] decoded = new byte[BLOCK_SIZE];
        if (padding > 0) {
            decoded = buildPaddingArray(padding);
        }
        String query; // requête représenté en chaine hexadécimale
        byte[] bquery; // requete sous forme byte array
        String hexBloc = toHexFromByteArray(ciphertext);

        for (int pos = BLOCK_SIZE - padding; pos > 0; pos--) {
            for (int guess = 0; guess < 256; guess++) {
                bquery = buildGuessForPosition(iv, decoded, pos - 1, (byte) guess);
                query = toHexFromByteArray(bquery) + hexBloc;
                if (poq.query(query)) {
                    decoded[pos - 1] = (byte) guess;
                    System.out.println(toPrintableString(decoded));
                    break;
                }
            }

        }
        return decoded;
    }

    public static void main(String[] args) {
        OraclePaddingClient opc = new OraclePaddingClient();
        PaddingOracleQuery opq = new PaddingOracleQuery();
        try {
            System.out.println("Server responded : " + opq.query(ENCRYPTED_MESSAGE));
        } catch (Exception e) {
            System.out.print("Exception caught. Server down ?");
            e.printStackTrace();
        }
        try {

            byte[][] messageblocks = opc.splitMessageIntoBlocks(toByteArrayFromHex(ENCRYPTED_MESSAGE));
            String printableresult = "";
            System.out.println("Message cut into blocks :");
            for (int i = 0; i < messageblocks.length; i++) {
                System.out.println(toHexFromByteArray(messageblocks[i]));
            }
            String hexresult = "";
            int padlen;

            for (int i = 0; i < messageblocks.length - 1; i++) {

                if (i == messageblocks.length - 2) {
                    System.out.println("I : " + String.valueOf(i));
                    padlen = opc.getPaddingLengthForLastBlock(opq, messageblocks[i], messageblocks[i + 1]);
                } else {
                    padlen = 0;
                }
                System.out.println("Length for padding : " + String.valueOf(padlen));
                byte[] result = opc.runDecryptionForBlock(opq, messageblocks[i], messageblocks[i + 1], padlen);
                String partialByteArrayResult = toPrintableString(result);
                String partialHexArrayResult = toHexFromByteArray(result);

                printableresult += partialByteArrayResult;
                hexresult += partialHexArrayResult;

                System.out.print("Bloc décodé :");
                System.out.println(partialByteArrayResult);
            }
            System.out.println("Final Result :");
            System.out.println(printableresult);
            System.out.println(hexresult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

