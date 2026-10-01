package br.com.convite.config;

import br.com.convite.gateway.persistence.UsuarioRepository;
import br.com.convite.gateway.persistence.entity.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final br.com.convite.gateway.persistence.ConviteCasamentoRepository conviteRepository;
    private final br.com.convite.gateway.persistence.ParticipanteCerimoniaRepository participanteRepository;
    private final br.com.convite.gateway.persistence.FornecedorCasamentoRepository fornecedorRepository;
    private final br.com.convite.gateway.persistence.PapelParticipanteRepository papelRepository;
    private final br.com.convite.gateway.persistence.VinculoParticipanteRepository vinculoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:Balboa@2027#N}")
    private String adminPassword;

    @Value("${RECEPCAO_PASSWORD:Recepcao@2027!}")
    private String recepcaoPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Garante as novas senhas definidas
        String finalAdminPass = "Balboa@2027#N";
        if (adminPassword != null && !adminPassword.isBlank() 
                && !adminPassword.equals("03112023") 
                && !adminPassword.equals("admin123")
                && !adminPassword.equals("Balboa@2027")) {
            finalAdminPass = adminPassword.trim();
        }

        String finalRecepcaoPass = "Recepcao@2027!";
        if (recepcaoPassword != null && !recepcaoPassword.isBlank()
                && !recepcaoPassword.equals("recepcao123")
                && !recepcaoPassword.equals("recepcao2027")) {
            finalRecepcaoPass = recepcaoPassword.trim();
        }

        // 1. Usuário Administrador (Painel dos Noivos)
        List<UsuarioEntity> admins = usuarioRepository.findAllByUsernameIgnoreCase("admin");

        if (admins.isEmpty()) {
            UsuarioEntity admin = UsuarioEntity.builder()
                    .id("1")
                    .username("admin")
                    .role("ADMIN")
                    .password(passwordEncoder.encode(finalAdminPass))
                    .build();
            usuarioRepository.save(admin);
            log.info("Inicializacao de dados concluida.");
        } else {
            UsuarioEntity admin = admins.get(0);
            if (admins.size() > 1) {
                for (int i = 1; i < admins.size(); i++) {
                    usuarioRepository.delete(admins.get(i));
                }
                log.info("Inicializacao de dados concluida.");
            }

            // Sincroniza e garante incondicionalmente a senha atualizada no banco de dados
            admin.setUsername("admin");
            admin.setRole("ADMIN");
            admin.setPassword(passwordEncoder.encode(finalAdminPass));
            usuarioRepository.save(admin);
            log.info("Inicializacao de dados concluida.");
        }

        // 2. Usuário de Recepção (Portaria)
        List<UsuarioEntity> recepcoes = usuarioRepository.findAllByUsernameIgnoreCase("recepcao");

        if (recepcoes.isEmpty()) {
            UsuarioEntity recepcao = UsuarioEntity.builder()
                    .id("2")
                    .username("recepcao")
                    .role("RECEPCAO")
                    .password(passwordEncoder.encode(finalRecepcaoPass))
                    .build();
            usuarioRepository.save(recepcao);
            log.info("Inicializacao de dados concluida.");
        } else {
            UsuarioEntity recepcao = recepcoes.get(0);
            if (recepcoes.size() > 1) {
                for (int i = 1; i < recepcoes.size(); i++) {
                    usuarioRepository.delete(recepcoes.get(i));
                }
                log.info("Inicializacao de dados concluida.");
            }

            // Sincroniza e garante incondicionalmente a senha atualizada no banco de dados
            recepcao.setUsername("recepcao");
            recepcao.setRole("RECEPCAO");
            recepcao.setPassword(passwordEncoder.encode(finalRecepcaoPass));
            usuarioRepository.save(recepcao);
            // DataInitializer: apenas usuários são inicializados, conforme solicitado.
            // As collections de convites, participantes e fornecedores ficam livres para o cadastro real.
        }
    }

    private void inicializarConvitesDemonstracao() {
        if (conviteRepository.count() == 0) {
            var convite1 = br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity.builder()
                    .codigo("fulana")
                    .familia("Fulana da Silva e Família")
                    .telefone("(11) 98888-7777")
                    .status("PENDENTE")
                    .createdAt(java.time.LocalDateTime.now())
                    .membros(List.of(
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Fulana da Silva")
                                    .criancaAte6Anos(false)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Lucas Silva (Filho)")
                                    .criancaAte6Anos(true)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Matheus Silva (Filho)")
                                    .criancaAte6Anos(false)
                                    .build()
                    ))
                    .build();

            var convite2 = br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity.builder()
                    .codigo("padrinhos-joao")
                    .familia("João e Mariana (Padrinhos)")
                    .telefone("(11) 97777-6666")
                    .status("PENDENTE")
                    .createdAt(java.time.LocalDateTime.now())
                    .membros(List.of(
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("João Pedro Santos")
                                    .papel("Padrinho")
                                    .par("Mariana Alencar")
                                    .criancaAte6Anos(false)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Mariana Alencar")
                                    .papel("Madrinha")
                                    .par("João Pedro Santos")
                                    .criancaAte6Anos(false)
                                    .build()
                    ))
                    .build();

            var convite3 = br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity.builder()
                    .codigo("fam-vasconcelos")
                    .familia("Família Vasconcelos (Pais do Noivo)")
                    .telefone("(11) 99999-5555")
                    .status("PENDENTE")
                    .createdAt(java.time.LocalDateTime.now())
                    .membros(List.of(
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Carlos Vasconcelos")
                                    .papel("Pai do Noivo")
                                    .par("Clara Vasconcelos")
                                    .criancaAte6Anos(false)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Clara Vasconcelos")
                                    .papel("Mãe do Noivo")
                                    .par("Carlos Vasconcelos")
                                    .criancaAte6Anos(false)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Sofia Vasconcelos")
                                    .papel("Daminha / Família")
                                    .criancaAte6Anos(true)
                                    .build()
                    ))
                    .build();

            var convite4 = br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity.builder()
                    .codigo("misael")
                    .familia("Misael & Família (Padrinho)")
                    .telefone("(11) 98888-1122")
                    .status("PENDENTE")
                    .createdAt(java.time.LocalDateTime.now())
                    .membros(List.of(
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Misael")
                                    .papel("Padrinho")
                                    .par("Anie")
                                    .criancaAte6Anos(false)
                                    .build(),
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Dona Lúcia (Mãe do Misael)")
                                    .criancaAte6Anos(false)
                                    .build()
                    ))
                    .build();

            var convite5 = br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity.builder()
                    .codigo("anie")
                    .familia("Anie Oliveira (Madrinha)")
                    .telefone("(11) 97777-3344")
                    .status("PENDENTE")
                    .createdAt(java.time.LocalDateTime.now())
                    .membros(List.of(
                            br.com.convite.gateway.persistence.entity.MembroConviteEntity.builder()
                                    .id(java.util.UUID.randomUUID())
                                    .nome("Anie")
                                    .papel("Madrinha")
                                    .par("Misael")
                                    .criancaAte6Anos(false)
                                    .build()
                    ))
                    .build();

            conviteRepository.saveAll(List.of(convite1, convite2, convite3, convite4, convite5));
            log.info("Inicializacao de dados concluida.");
        }
    }

    private void inicializarParticipantesDemonstracao() {
        if (participanteRepository.count() == 0) {
            var agora = java.time.LocalDateTime.now();
            var p1 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Maria Santos")
                    .papel("Madrinha")
                    .vinculo("Noivo")
                    .par("Lucas Santos")
                    .telefone("(11) 98777-1111")
                    .codigoConvite("padrinhos-joao")
                    .confirmadoRsvp(true)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p2 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("João Pedro Santos")
                    .papel("Padrinho")
                    .vinculo("Noivo")
                    .par("Mariana Alencar")
                    .telefone("(11) 97777-6666")
                    .codigoConvite("padrinhos-joao")
                    .confirmadoRsvp(true)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p3 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Mariana Alencar")
                    .papel("Madrinha")
                    .vinculo("Noiva")
                    .par("João Pedro Santos")
                    .telefone("(11) 97777-6666")
                    .codigoConvite("padrinhos-joao")
                    .confirmadoRsvp(false)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p4 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Carlos Vasconcelos")
                    .papel("Pai do Noivo")
                    .vinculo("Noivo")
                    .par("Clara Vasconcelos")
                    .telefone("(11) 99999-5555")
                    .codigoConvite("fam-vasconcelos")
                    .confirmadoRsvp(true)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p5 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Clara Vasconcelos")
                    .papel("Mãe do Noivo")
                    .vinculo("Noivo")
                    .par("Carlos Vasconcelos")
                    .telefone("(11) 99999-5555")
                    .codigoConvite("fam-vasconcelos")
                    .confirmadoRsvp(true)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p6 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Misael")
                    .papel("Padrinho")
                    .vinculo("Noivo")
                    .par("Anie")
                    .telefone("(11) 98888-1122")
                    .codigoConvite("misael")
                    .confirmadoRsvp(false)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            var p7 = br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity.builder()
                    .nome("Anie")
                    .papel("Madrinha")
                    .vinculo("Noiva")
                    .par("Misael")
                    .telefone("(11) 97777-3344")
                    .codigoConvite("anie")
                    .confirmadoRsvp(false)
                    .presenteCheckin(false)
                    .createdAt(agora)
                    .build();

            participanteRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7));
            log.info("Participantes de cerimonia de demonstracao inicializados.");
        }
    }

    private void inicializarFornecedoresDemonstracao() {
        if (fornecedorRepository.count() == 0) {
            var agora = java.time.LocalDateTime.now();
            var f1 = br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity.builder()
                    .nome("Luciano")
                    .responsavel("Luciano")
                    .papel("Fornecedor")
                    .categoria("Música & Som")
                    .servico("Orquestra da Cerimônia")
                    .empresa("Harmonia Musical")
                    .telefone("(11) 98111-2233")
                    .horarioPrevisto("14:00")
                    .instrucaoChegada("Chegada antecipada às 14:00 para afinação e montagem de instrumentos acústicos")
                    .chegadaAntecipada(true)
                    .equipe(new java.util.ArrayList<>(List.of(
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f1-1").nome("Luciano").funcao("Maestro / Responsável").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f1-2").nome("Amanda").funcao("Violino").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f1-3").nome("Felipe").funcao("Violoncelo").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f1-4").nome("Mariana").funcao("Teclado").presente(false).build()
                    )))
                    .createdAt(agora)
                    .build();

            var f2 = br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity.builder()
                    .nome("Camila")
                    .responsavel("Camila")
                    .papel("Fornecedor")
                    .categoria("Foto & Vídeo")
                    .servico("Fotografia & Vídeo")
                    .empresa("Studio Lumière")
                    .telefone("(11) 98222-3344")
                    .horarioPrevisto("14:30")
                    .instrucaoChegada("Chegada antecipada para início da cobertura de making-of e decoração")
                    .chegadaAntecipada(true)
                    .equipe(new java.util.ArrayList<>(List.of(
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f2-1").nome("Camila").funcao("Fotógrafa Principal").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f2-2").nome("Pedro").funcao("Cinegrafista").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f2-3").nome("Lucas").funcao("Assistente de Luz").presente(false).build()
                    )))
                    .createdAt(agora)
                    .build();

            var f3 = br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity.builder()
                    .nome("DJ Rodrigo")
                    .responsavel("DJ Rodrigo")
                    .papel("Fornecedor")
                    .categoria("Música & Som")
                    .servico("Som e Iluminação")
                    .empresa("Beat & Light")
                    .telefone("(11) 98333-4455")
                    .horarioPrevisto("13:00")
                    .instrucaoChegada("Montagem técnica antecipada de som de pista e iluminação cênica")
                    .chegadaAntecipada(true)
                    .equipe(new java.util.ArrayList<>(List.of(
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f3-1").nome("DJ Rodrigo").funcao("DJ e Operador").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f3-2").nome("Tiago").funcao("Técnico de Som").presente(false).build()
                    )))
                    .createdAt(agora)
                    .build();

            var f4 = br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity.builder()
                    .nome("Marcelo")
                    .responsavel("Marcelo")
                    .papel("Fornecedor")
                    .categoria("Buffet & Gastronomia")
                    .servico("Buffet Completo & Bar")
                    .empresa("Gastronomia Imperial")
                    .telefone("(11) 98444-5566")
                    .horarioPrevisto("12:00")
                    .instrucaoChegada("Cozinha e bar liberados a partir das 12:00 para preparo e mise en place")
                    .chegadaAntecipada(true)
                    .equipe(new java.util.ArrayList<>(List.of(
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f4-1").nome("Marcelo").funcao("Chef Executivo").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f4-2").nome("Renata").funcao("Maître").presente(false).build(),
                            br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity.builder()
                                    .id("f4-3").nome("Gustavo").funcao("Chefe de Bar").presente(false).build()
                    )))
                    .createdAt(agora)
                    .build();

            fornecedorRepository.saveAll(List.of(f1, f2, f3, f4));
            log.info("Inicializacao de dados concluida.");
        }
    }

    private void inicializarClassificacoesDemonstracao() {
        var agora = java.time.LocalDateTime.now();
        if (papelRepository.count() == 0) {
            var papeis = List.of(
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Convidado").cortejo(false).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Padrinho").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Madrinha").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Pai").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Mãe").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Daminha").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Pajem").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Florista").cortejo(true).createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.PapelParticipanteEntity.builder().nome("Outro").cortejo(false).createdAt(agora).updatedAt(agora).build()
            );
            papelRepository.saveAll(papeis);
            log.info("Papeis padrao inicializados com sucesso.");
        } else {
            // Migração de bancos existentes: renomeia 'Convidado comum' -> 'Convidado' e remove 'Cortejo' como papel
            papelRepository.findByNomeIgnoreCase("Convidado comum").ifPresent(p -> {
                p.setNome("Convidado");
                p.setCortejo(false);
                p.setUpdatedAt(agora);
                papelRepository.save(p);
            });
            papelRepository.findByNomeIgnoreCase("Cortejo").ifPresent(papelRepository::delete);
        }

        if (vinculoRepository.count() == 0) {
            var vinculos = List.of(
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Noivo").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Noiva").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Pai/Mãe").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Irmão/Irmã").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Família").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Amigo(a)").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Colega").createdAt(agora).updatedAt(agora).build(),
                    br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity.builder().nome("Outro").createdAt(agora).updatedAt(agora).build()
            );
            vinculoRepository.saveAll(vinculos);
            log.info("Vinculos padrao inicializados com sucesso.");
        }
    }
}
