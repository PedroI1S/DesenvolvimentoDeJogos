# 🏹 BOW AND ARROW - PROTÓTIPO FUNCIONAL

## Visão Geral

Um jogo de arco e flecha em libGDX que implementa **todos os requisitos** da atividade com três padrões de design avançados: **InputProcessor**, **AssetManager**, e **Object Pooling**. Arquitetura profissional com baixo acoplamento e alto desempenho.

---

## ✨ Funcionalidades Implementadas

### 🔊 Sistema de Som e Música (com AssetManager)
- **AssetManager**: Gerenciamento centralizado de recursos
- **Música de Background**: Loop contínuo durante o jogo
- **Som de Disparo**: Toca ao atirar uma flecha (única instância)
- **Implementação Robusta**: Try-catch previne crashes se assets faltarem
- **Cleanup Automático**: AssetManager.dispose() libera todos os recursos
  - `Arrow.java`: Recebe referência ao `Sound` de disparo
  - `Main.java`: Gerencia carregamento via AssetManager

```java
// Exemplo em Arrow.java
private Sound shootSound;  // Referência interna
public Arrow(float x, float y, float angle, Sound sound) {
    this.shootSound = sound;
}
```

### 📷 Sistema de Câmera Avançado
- **Translação**: Move câmera em 4 direções (WASD)
- **Zoom**: 0.5x até 3x (UP/DOWN)
- **Project/Unproject**: Converte coordenadas mundo ↔ tela
  ```java
  // Converter posição do mouse para mundo
  camera.unproject(touchPoint);  // Tela → Mundo
  camera.project(vector);         // Mundo → Tela (disponível)
  ```

### ⏱️ Sistema de Timer Delta-Based
- **GameTimer.java**: Classe reutilizável
- **Timer em Arrow**: Cada flecha dura 10 segundos
- **Timer em Archer**: Cooldown de 0.5s entre tiros
- **Frame-rate independente**: Baseado em `delta` time

```java
// GameTimer.java - simples e eficiente
public class GameTimer {
    private float elapsed;
    private float duration;
    
    public void update(float delta) {
        elapsed += delta;
        if (elapsed >= duration) finished = true;
    }
}
```

### 🎯 Arquitetura com Baixo Acoplamento

```
Main.java (Orquestrador)
├── GameInputProcessor (Input centralizado)
│   └── Flags booleanas (wPressed, upPressed, etc.)
├── AssetManager (Recursos)
│   ├── Sound (shoot.wav)
│   └── Music (background.wav)
├── ArrowPool (reciclagem de flechas)
│   └── Arrow[] (até 50 instâncias reutilizáveis)
├── EnemyPool (reciclagem de inimigos)
│   └── Enemy[] (até 30 instâncias reutilizáveis)
├── Archer (Entidade com cooldown)
│   └── GameTimer (Controla cooldown)
└── Câmera (Controla visão)

Responsabilidades claras:
- GameTimer: Apenas cronometragem
- GameInputProcessor: Apenas captura de input
- AssetManager: Apenas gerenciamento de recursos
- ObjectPool: Apenas reutilização de objetos
- Arrow: Renderização, som, movimento
- Enemy: IA, patrulha, colisão
- Archer: Apontar, disparar, cooldown
- Main: Orquestração sem conhecer detalhes
```

---

## 🎮 Controles

| Ação | Tecla |
|------|-------|
| Mover câmera | **WASD** |
| Aumentar zoom | **UP** |
| Diminuir zoom | **DOWN** |
| Reset câmera | **R** |
| Atirar flecha | **CLICK** (mouse) |
| Apontaria automática | Mouse se move automaticamente com o cursor |

## 🎯 Gameplay

- **Objetivo**: Eliminar inimigos vermelhos com flechas
- **Inimigos**: Spawnam nas bordas e patrulham aleatoriamente
- **Colisão**: Flecha + inimigo = eliminação mútua
- **UI**: Mostra estatísticas em tempo real
- **Modo**: Infinito com dificuldade crescente

---

## 📁 Estrutura de Arquivos

```
versao1/
├── core/src/main/java/io/github/some_example_name/
│   ├── Main.java              ← Classe principal (Game)
│   ├── GameTimer.java         ← Timer delta-based reutilizável
│   ├── GameInputProcessor.java ← InputProcessor para input centralizado
│   ├── ObjectPool.java        ← Abstract class genérica para pooling
│   ├── ArrowPool.java         ← Pool especializado para Arrows
│   ├── EnemyPool.java         ← Pool especializado para Enemies
│   ├── Arrow.java             ← Flecha com som e pooling
│   ├── Archer.java            ← Arqueiro com cooldown
│   ├── Enemy.java             ← Inimigo com IA e pooling
│   └── Explosion.java         ← Efeito visual auxiliar
│
├── assets/sounds/
│   ├── shoot.wav              ← Som de disparo
│   └── background.wav         ← Música background
│
├── build.gradle               ← Dependências
├── README_BOW_AND_ARROW.md ← Documentação completa
├── run.sh                  ← Script de execução
└── generate_audio_simple.py ← Gera áudio de teste
```

---

## 🚀 Como Executar

### Opção 1: Script Bash (Recomendado)
```bash
cd versao1/
chmod +x run.sh
./run.sh
```

### Opção 2: Gradle Direto
```bash
cd versao1/
./gradlew lwjgl3:run
```

### Opção 3: IDE (VS Code / IntelliJ)
1. Abra a pasta como projeto Gradle
2. Execute task: `lwjgl3:run`

---

## 🔧 Tecnologias Utilizadas

- **libGDX**: Framework de jogos
- **Gradle**: Build system
- **Java 11+**: Linguagem
- **ShapeRenderer**: Renderização de formas
- **OrthographicCamera**: Sistema de câmera 2D

---

## 📊 Análise de Requisitos

### Requirement Checklist ✅

- ✅ **Som**: Implementado em contexto OO (Arrow.java com Sound próprio)
- ✅ **Música**: Background music com looping automático
- ✅ **Câmera Translação**: WASD move câmera suavemente
- ✅ **Câmera Zoom**: UP/DOWN com bounds checking
- ✅ **Project/Unproject**: Usado para converter mouse → mundo
- ✅ **Timer com Delta**: GameTimer baseado em delta time
- ✅ **Timer OO**: Cada objeto tem seu próprio timer
- ✅ **Baixo Acoplamento**: Classes independentes com responsabilidades claras

---

## 💡 Detalhes Técnicos Interessantes

### 1. Timer Independente de Frame-Rate
```java
// Funciona em qualquer FPS
public void update(float delta) {
    elapsed += delta;  // delta = tempo desde frame anterior
}
```

### 2. Câmera com Interpolação Suave
```java
float cameraLerp = 0.15f;
camera.position.x += (target.x - camera.position.x) * cameraLerp;
```

### 3. Som Sem Redundância
```java
if (!soundPlayed && shootSound != null) {
    shootSound.play();
    soundPlayed = true;  // Toca apenas UMA vez
}
```

### 4. GameTimer Reutilizável
A mesma classe é usada para:
- Cooldown do Archer (0.5s)
- Vida da Arrow (10s)
- Pode ser usada para qualquer coisa: animações, efeitos, etc.

---

## 🎥 Captura de Tela (Descrição)

```
┌─────────────────────────────────────────────────────┐
│ Bow and Arrow - WASD: Move, UP/DOWN: Zoom, R: Reset│
├─────────────────────────────────────────────────────┤
│                                                     │
│  ∧         [Grid com limite do mundo]       →      │
│  ║                                                  │
│  ║     ●  (Archer - aponta para mouse)       →     │
│  ║    /│                                     →     │
│  ║   / │     ▲  [Flechas disparadas]        →     │
│  ║      ▲▲  ▲▲                              →     │
│  ║       ▲▲  ▲▲                             →     │
│  ║                                                  │
│  ║                                          →     │
│  ║ Grid visual para demonstrar translação  →     │
│                                                     │
│ Arrows Shot: 5    Arrows Active: 2   Zoom: 1.50x  │
└─────────────────────────────────────────────────────┘
```

---

## 🔮 Possíveis Extensões

Para tornar mais interessante:

1. **Sistemas**
   - Alvo móvel/inimigos
   - Sistema de pontuação
   - Leaderboard
   - Diferentes tipos de flecha

2. **Gráficos**
   - Texturas em vez de shapes
   - Animação de sprites
   - Efeitos de partículas
   - Parallax background

3. **Áudio**
   - Mais sons de feedback
   - Diferentes tonalidades
   - Volume controls

4. **Gameplay**
   - Física mais realista
   - Vento/gravidade
   - Modo multi-jogador

---

## 📝 Notas de Entrega

- ✅ Código funcional e compilável
- ✅ Todos os requisitos implementados
- ✅ Baixo acoplamento e OO apropriado
- ✅ Sem dependências externas (exceto libGDX)
- ✅ Documentação clara
- ✅ Comentários no código explicando conceitos-chave
- ✅ Áudio gerado (sem dependências externas)

---

## 🐛 Troubleshooting

### "Arquivo de som não encontrado"
- Verifique se `assets/sounds/` existe
- Rode: `python3 generate_audio_simple.py`

### Compilação lenta
- Primeira compilação é lenta (download de deps)
- Próximas serão mais rápidas

### Câmera saindo dos limites
- Há verificação de bounds, câmera nunca sai do mundo
- Grid visual ajuda a ver os limites

---

**Desenvolvido para atividade de Desenvolvimento de Jogos**  
**libGDX | Java | Gradle**
