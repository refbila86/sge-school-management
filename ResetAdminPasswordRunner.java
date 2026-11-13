package mz.co.sge.config;

import mz.co.sge.entity.UserEntity;
import mz.co.sge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Script temporário para resetar a senha do admin para admin123 com BCrypt.
 * 
 * COMO USAR:
 * 1. Cole esta classe em src/main/java/mz/co/sge/config/
 * 2. Rode a aplicação com o profile: reset-admin
 *    Ou adicione --reset.admin=true nas VM options
 * 3. Veja no console o hash gerado e a confirmação
 * 4. APAGUE esta classe depois de usar!
 */
@Component
public class ResetAdminPasswordRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetAdminPasswordRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Só executa se passar o argumento --reset.admin=true
        boolean shouldRun = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--reset.admin=true") || arg.equalsIgnoreCase("reset-admin")) {
                shouldRun = true;
                break;
            }
        }

        if (!shouldRun) {
            // Descomenta a linha abaixo se quiser que rode sempre na inicialização (apenas para teste)
            // shouldRun = true;
            return;
        }

        String rawPassword = "admin123";
        String encoded = passwordEncoder.encode(rawPassword);

        System.out.println("=============================================");
        System.out.println("GERANDO HASH BCRYPT PARA: " + rawPassword);
        System.out.println("HASH: " + encoded);
        System.out.println("=============================================");

        // Tenta achar o admin - ajusta o schoolId se precisares
        Optional<UserEntity> adminOpt = userRepository.findByUsernameAndSchoolId("admin", 1L);
        
        // Se não achar com schoolId 1, tenta sem filtro
        if (adminOpt.isEmpty()) {
            adminOpt = userRepository.findAll().stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase("admin"))
                    .findFirst();
        }

        if (adminOpt.isEmpty()) {
            System.out.println("--> ERRO: Utilizador 'admin' não encontrado no BD!");
            System.out.println("Lista de users: ");
            userRepository.findAll().forEach(u -> System.out.println(" - " + u.getUsername() + " | school=" + u.getSchoolId()));
            return;
        }

        UserEntity admin = adminOpt.get();
        System.out.println("Utilizador encontrado: ID=" + admin.getId() + " | username=" + admin.getUsername() + " | school=" + admin.getSchoolId());
        System.out.println("Hash antigo: " + admin.getPassword());
        System.out.println("Tamanho hash antigo: " + (admin.getPassword() != null ? admin.getPassword().length() : 0));

        admin.setPassword(encoded);
        admin.setActive(true);
        userRepository.save(admin);

        // Teste de validação
        boolean matches = passwordEncoder.matches(rawPassword, encoded);
        System.out.println("Validação matches(admin123, hash): " + matches);
        System.out.println("--> SUCESSO: Senha do admin atualizada para 'admin123'");
        System.out.println("=============================================");
    }
}
