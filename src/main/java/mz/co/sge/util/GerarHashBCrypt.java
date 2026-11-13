package mz.co.sge.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHashBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String raw = "admin123";
        
        String hash = encoder.encode(raw);
        
        System.out.println("=====================================");
        System.out.println("Senha original: " + raw);
        System.out.println("Hash BCrypt: " + hash);
        System.out.println("Tamanho: " + hash.length());
        System.out.println("Teste matches: " + encoder.matches(raw, hash));
        System.out.println("=====================================");
        System.out.println("");
        System.out.println("COPIA E RODA ESTE SQL:");
        System.out.println("UPDATE user SET password = '" + hash + "', active = true WHERE username = 'admin';");
    }
}