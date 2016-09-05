package com.secure.chat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

/**
 * Created by goodbytes on 9/4/2016.
 */
public class KeyGen {

    private KeyPair pair;

    public KeyGen() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        generateKeys();
    }

    public PublicKey getPublicKey(){
        return pair.getPublic();
    }

    public PrivateKey getPrivateKey(){
        return pair.getPrivate();
    }

    private void saveKeys(PrivateKey priv, PublicKey pub) throws IOException {
        /* save the public key in a file */
        byte[] key;
        FileOutputStream keyfos;

        key = pub.getEncoded();
        keyfos = new FileOutputStream("pubkey");
        keyfos.write(key);
        keyfos.close();

        key = priv.getEncoded();
        keyfos = new FileOutputStream("privkey");
        keyfos.write(key);
        keyfos.close();
    }

    public void generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstanceStrong();
        keyGen.initialize(4096, random);
        pair = keyGen.generateKeyPair();
        //saveKeys(pair.getPrivate(), pair.getPublic());
    }
}
