package br.com.convite.config;

import br.com.convite.gateway.persistence.PresenteRepository;
import br.com.convite.gateway.persistence.UsuarioRepository;
import br.com.convite.gateway.persistence.entity.PresenteEntity;
import br.com.convite.gateway.persistence.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PresenteRepository presenteRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Inicializa o admin se não existir
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(UsuarioEntity.builder()
                    .id(1L)
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .build());
            System.out.println("✅ Usuário admin criado com a senha segura das variáveis de ambiente.");
        }

        // Limpa os presentes antigos para garantir a nova lista
        presenteRepository.deleteAll();

        if (presenteRepository.count() == 0) {
            List<PresenteEntity> presentes = List.of(
                // Cama, Mesa e Banho
                createPresente(1L, "Jogo de Toalhas de Banho", "Cama e Banho", "Branco", 2, "./images/cat_cama.jpg"),
                createPresente(2L, "Jogo de Lençol Casal", "Cama e Banho", "Branco", 2, "./images/cat_cama.jpg"),
                
                // Servir e Mesa Posta
                createPresente(3L, "Aparelho de Jantar", "Servir", "Branco", 2, "./images/cat_servir.jpg"),
                createPresente(4L, "Jogo de Taças de Água", "Servir", "Creme", 2, "./images/cat_servir.jpg"),
                createPresente(5L, "Jogo de Taças de Vinho", "Servir", "Creme", 2, "./images/cat_servir.jpg"),
                createPresente(6L, "Xícaras de Chá com Pires", "Servir", "Rosa Antigo", 2, "./images/cat_servir.jpg"),
                createPresente(7L, "Potes Herméticos de Vidro", "Utensílios", "Branco", 2, "./images/cat_utensilios.jpg"),

                // Itens Únicos ou Eletrodomésticos
                createPresente(8L, "Faqueiro Dourado", "Servir", "Dourado", 1, "./images/cat_servir.jpg"),
                createPresente(9L, "Jarra de Cristal", "Servir", "Creme", 1, "./images/cat_servir.jpg"),
                
                // Panelas e Cocção
                createPresente(10L, "Jogo de Panelas Antiaderentes", "Panelas", "Rosa", 2, "./images/cat_utensilios.jpg"),
                createPresente(11L, "Panela de Pressão", "Panelas", "Creme", 1, "./images/cat_utensilios.jpg"),
                createPresente(12L, "Conjunto de Assadeiras", "Panelas", "Rosa", 1, "./images/cat_utensilios.jpg"),

                // Utensílios de Preparo
                createPresente(13L, "Jogo de Facas do Chef", "Utensílios", "Preto", 1, "./images/cat_utensilios.jpg"),
                createPresente(14L, "Tábua de Corte Profissional", "Utensílios", "Dourado", 1, "./images/cat_utensilios.jpg"),
                createPresente(15L, "Kit Utensílios de Silicone", "Utensílios", "Rosa", 1, "./images/cat_utensilios.jpg"),
                createPresente(16L, "Escorredor de Louça", "Utensílios", "Dourado", 1, "./images/cat_utensilios.jpg"),
                createPresente(17L, "Porta-Temperos Giratório", "Utensílios", "Branco", 1, "./images/cat_utensilios.jpg"),

                // Eletroportáteis
                createPresente(18L, "Chaleira Elétrica", "Eletrodomésticos", "Creme", 1, "./images/cat_utensilios.jpg"),
                createPresente(19L, "Liquidificador Retrô", "Eletrodomésticos", "Branco", 1, "./images/cat_utensilios.jpg")
            );
            presenteRepository.saveAll(presentes);
            System.out.println("✅ " + presentes.size() + " itens únicos inseridos (capacidade lógica para 26 reservas).");
        }
    }

    private PresenteEntity createPresente(Long id, String nome, String categoria, String cor, int maxQtd, String imagem) {
        return PresenteEntity.builder()
                .id(id)
                .nome(nome)
                .imagem(imagem)
                .categoria(categoria)
                .cor(cor)
                .descricao("Um item especial escolhido com muito carinho para a nossa casa.")
                .linkCompra("")
                .maxQuantity(maxQtd)
                .activeReservationsCount(0)
                .esgotado(false)
                .build();
    }
}
