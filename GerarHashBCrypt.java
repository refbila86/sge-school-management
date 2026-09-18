import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Classe standalone para gerar hash BCrypt sem precisar subir o projeto inteiro.
 * 
 * COMO USAR no teu projeto:
 * 1. Copia este arquivo para a raiz do projeto
 * 2. No terminal, na pasta do projeto, roda:
 *    mvn exec:java -Dexec.mainClass="GerarHashBCrypt"
 *    
 * OU cria uma classe main simples no teu IDE e roda.
 */
public class GerarHashBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String raw = "admin123";
        
        // Gera 2 hashes diferentes (BCrypt gera salt diferente sempre)
        String hash1 = encoder.encode(raw);
        String hash2 = encoder.encode(raw);
        
        System.out.println("Senha original: " + raw);
        System.out.println("Hash 1 (use este no SQL): " + hash1);
        System.out.println("Hash 2 (alternativo): " + hash2);
        System.out.println("Tamanho: " + hash1.length());
        System.out.println("Validação: " + encoder.matches(raw, hash1));
        System.out.println("");
        System.out.println("SQL para atualizar:");
        System.out.println("UPDATE users SET password = '" + hash1 + "' WHERE username = 'admin';");
        System.out.println("");
        System.out.println("Se tiver coluna school_id:");
        System.out.println("UPDATE users SET password = '" + hash1 + "' WHERE username = 'admin' AND school_id = 1;");
    }
}
