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
                // Cama, Mesa e Banho (Itens de uso diário intenso -> Qtd: 2)
                createPresente(1L, "Jogo de Toalhas de Banho", "Cama e Banho", 2, "https://image.pollinations.ai/prompt/plush%20vintage%20pastel%20bath%20towels%20set%20folded%20elegantly?width=600&height=600&nologo=true"),
                createPresente(2L, "Jogo de Lençol Casal", "Cama e Banho", 2, "https://image.pollinations.ai/prompt/elegant%20vintage%20bed%20sheets%20set%20pastel%20colors?width=600&height=600&nologo=true"),
                
                // Servir e Mesa Posta (Itens que formam conjuntos maiores -> Qtd: 2)
                createPresente(3L, "Aparelho de Jantar", "Servir", 2, "https://image.pollinations.ai/prompt/elegant%20porcelain%20dinner%20plates%20with%20gold%20rims?width=600&height=600&nologo=true"),
                createPresente(4L, "Jogo de Taças de Água", "Servir", 2, "https://image.pollinations.ai/prompt/crystal%20water%20glasses%20set,%20elegant%20table%20setting?width=600&height=600&nologo=true"),
                createPresente(5L, "Jogo de Taças de Vinho", "Servir", 2, "https://image.pollinations.ai/prompt/crystal%20wine%20glasses%20set,%20elegant%20vintage%20table?width=600&height=600&nologo=true"),
                createPresente(6L, "Xícaras de Chá com Pires", "Servir", 2, "https://image.pollinations.ai/prompt/vintage%20porcelain%20teacups%20and%20saucers%20with%20gold%20details?width=600&height=600&nologo=true"),
                createPresente(7L, "Potes Herméticos de Vidro", "Utensílios", 2, "https://image.pollinations.ai/prompt/glass%20food%20storage%20containers%20bamboo%20lids?width=600&height=600&nologo=true"),

                // Itens Únicos ou Eletrodomésticos (Não faz sentido ter mais de um -> Qtd: 1)
                createPresente(8L, "Faqueiro Dourado", "Servir", 1, "https://image.pollinations.ai/prompt/vintage%20gold%20cutlery%20set%20in%20a%20velvet%20box?width=600&height=600&nologo=true"),
                createPresente(9L, "Jarra de Cristal", "Servir", 1, "https://image.pollinations.ai/prompt/vintage%20crystal%20water%20pitcher%20jug?width=600&height=600&nologo=true"),
                
                // Panelas e Cocção (Itens valiosos, podem ter mais cotas/unidades)
                createPresente(10L, "Jogo de Panelas Antiaderentes", "Panelas", 2, "https://image.pollinations.ai/prompt/vintage%20pastel%20pink%20and%20gold%20cooking%20pots%20set?width=600&height=600&nologo=true"),
                createPresente(11L, "Panela de Pressão", "Panelas", 1, "https://image.pollinations.ai/prompt/vintage%20cream%20pressure%20cooker%20pot?width=600&height=600&nologo=true"),
                createPresente(12L, "Conjunto de Assadeiras", "Panelas", 1, "https://image.pollinations.ai/prompt/white%20ceramic%20rectangular%20baking%20dish,%20vintage?width=600&height=600&nologo=true"),

                // Utensílios de Preparo (Qtd: 1)
                createPresente(13L, "Jogo de Facas do Chef", "Utensílios", 1, "https://image.pollinations.ai/prompt/professional%20chef%20knives%20set%20wooden%20handles?width=600&height=600&nologo=true"),
                createPresente(14L, "Tábua de Corte Profissional", "Utensílios", 1, "https://image.pollinations.ai/prompt/premium%20walnut%20wood%20cutting%20board?width=600&height=600&nologo=true"),
                createPresente(15L, "Kit Utensílios de Silicone", "Utensílios", 1, "https://image.pollinations.ai/prompt/pastel%20pink%20silicone%20kitchen%20utensils?width=600&height=600&nologo=true"),
                createPresente(16L, "Escorredor de Louça", "Utensílios", 1, "https://image.pollinations.ai/prompt/rose%20gold%20dish%20drying%20rack?width=600&height=600&nologo=true"),
                createPresente(17L, "Porta-Temperos Giratório", "Utensílios", 1, "https://image.pollinations.ai/prompt/vintage%20spice%20rack%20glass%20jars?width=600&height=600&nologo=true"),

                // Eletroportáteis (Qtd: 1)
                createPresente(18L, "Chaleira Elétrica", "Eletrodomésticos", 1, "https://image.pollinations.ai/prompt/vintage%20pastel%20cream%20electric%20kettle?width=600&height=600&nologo=true"),
                createPresente(19L, "Liquidificador Retrô", "Eletrodomésticos", 1, "https://image.pollinations.ai/prompt/vintage%20pastel%20cream%20blender?width=600&height=600&nologo=true")
            );
            presenteRepository.saveAll(presentes);
            System.out.println("✅ " + presentes.size() + " itens únicos inseridos (capacidade lógica para 26 reservas).");
        }
    }

    private PresenteEntity createPresente(Long id, String nome, String categoria, int maxQtd, String imagem) {
        return PresenteEntity.builder()
                .id(id)
                .nome(nome)
                .imagem(imagem)
                .categoria(categoria)
                .cor("Variado")
                .descricao("Um item especial escolhido com muito carinho para a nossa casa.")
                .linkCompra("")
                .maxQuantity(maxQtd)
                .activeReservationsCount(0)
                .esgotado(false)
                .build();
    }
}
