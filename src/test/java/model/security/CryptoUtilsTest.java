package model.security;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    private SecretKey key16(String s) {
        return new SecretKeySpec(s.getBytes(), "AES");
    }

    // -------- Test encrypt()/decrypt() --------

    // {A1:chiave_valida, B1:testo_non_vuoto, C1:ciphertext_valido}
    @Test
    void encryptDecrypt_roundTrip_testoNonVuoto() throws Exception {
        SecretKey key = key16("0123456789abcdef");
        String original = "thisIsATestString123!@#";

        String encrypted = CryptoUtils.encrypt(key, original);
        String decrypted = CryptoUtils.decrypt(key, encrypted);

        assertEquals(original, decrypted);
    }

    // {A1:chiave_valida, B2:testo_vuoto, C1:ciphertext_valido}
    @Test
    void encryptDecrypt_roundTrip_testoVuoto() throws Exception {
        SecretKey key = key16("0123456789abcdef");

        String encrypted = CryptoUtils.encrypt(key, "");
        String decrypted = CryptoUtils.decrypt(key, encrypted);

        assertEquals("", decrypted);
    }

    // {A1:chiave_valida, B3:testo_unicode, C1:ciphertext_valido}
    @Test
    void encryptDecrypt_roundTrip_unicode() throws Exception {
        SecretKey key = key16("0123456789abcdef");
        String text = "thisIsAÜñîçødëТест字符串🚀";

        String encrypted = CryptoUtils.encrypt(key, text);
        String decrypted = CryptoUtils.decrypt(key, encrypted);

        assertEquals(text, decrypted);
    }

    // {A1:chiave_valida, B1:testo_non_vuoto, C1:ciphertext_valido_random}
    @Test
    void encrypt_dueEncryptDiversi_generanoCipherDiversi() throws Exception {
        SecretKey key = key16("0123456789abcdef");

        String c1 = CryptoUtils.encrypt(key, "mango");
        String c2 = CryptoUtils.encrypt(key, "mango");

        assertNotEquals(c1, c2); // IV random => ciphertext cambia
    }

    // {A2:chiave_diversa, B1:testo_non_vuoto, C1:ciphertext_valido}
    @Test
    void decrypt_conChiaveErrata_lanciaException() throws Exception {
        SecretKey keyCorrect = key16("0123456789abcdef");
        SecretKey keyWrong = key16("abcdef0123456789");

        String encrypted = CryptoUtils.encrypt(keyCorrect, "fratmango");

        assertThrows(Exception.class, () ->
                CryptoUtils.decrypt(keyWrong, encrypted)
        );
    }

    // {A1:chiave_valida, B1:testo_non_vuoto, C2:ciphertext_manomesso}
    @Test
    void decrypt_ciphertextCorrotto_lanciaException() {
        SecretKey key = key16("0123456789abcdef");

        String corrupted = "thisIsNotBase64!!";

        assertThrows(Exception.class, () ->
                CryptoUtils.decrypt(key, corrupted)
        );
    }
}
