# Bow and Arrow - Protótipo Minimalista

## Resumo do Projeto

Este é um protótipo funcional de um jogo de arco e flecha em libGDX que implementa todos os requisitos da atividade:

### ✅ Funcionalidades Implementadas

1. **🔊 Som e Música** (com AssetManager)
   - `AssetManager`: Carregamento centralizado de assets
   - Música de background contínua (looping)
   - Som de disparo toca uma única vez por evento
   - Implementado em contexto OO com gestão de recursos segura
   - Tratamento robusto de erros (try-catch)

2. **📷 Sistema de Câmera**
   - Translação: Use **WASD** para mover a câmera em 4 direções
   - Zoom: Use **UP/DOWN** para aumentar/diminuir zoom (0.5x a 3x)
   - Método `project` e `unproject` para converter coordenadas de mundo ↔ tela
   - Reset: Pressione **R** para voltar à posição padrão
   - Câmera segue suavemente com lerp (interpolação)

3. **⏱️ Timer Sistema**
   - Classe `GameTimer` reutilizável para qualquer objeto
   - Timer interno em `Arrow` (10 segundos de vida)
   - Timer interno em `Archer` (cooldown de 0.5s entre tiros)
   - Timer interno em `Enemy` (mudança de direção cada 3s)
   - Baseado em delta time (frame-rate independente)

4. **🎮 InputProcessor Centralizado**
   - `GameInputProcessor`: Implementa interface InputProcessor
   - Flags booleanas para cada entrada (wPressed, aPressed, upPressed, etc.)
   - Substituiu todas as chamadas diretas `Gdx.input.isKeyPressed()`
   - Facilita testes e reutilização

5. **♻️ Object Pooling para Performance**
   - `ArrowPool`: Reutiliza instâncias de Arrow (até 50)
   - `EnemyPool`: Reutiliza instâncias de Enemy (até 30)
   - Reduz garbage collection significativamente
   - Melhora consistência de frame rate

6. **🎯 Arquitetura OO com Baixo Acoplamento**
   - Cada classe tem responsabilidade única e clara
   - `GameTimer`: Apenas cronometragem
   - `GameInputProcessor`: Apenas captura de input
   - `ObjectPool`: Apenas gerenciamento de reutilização
   - `Arrow`, `Archer`, `Enemy`: Gerenciam sua própria lógica
   - `Main`: Orquestra sem conhecer detalhes internos

## Controles

- **WASD**: Mover câmera
- **UP/DOWN**: Aumentar/diminuir zoom
- **R**: Reset de câmera
- **MOUSE CLICK**: Atirar flecha (com cooldown de 0.5s)
- O arqueiro aponta automaticamente para a posição do mouse

## Gameplay

- **Objetivo**: Atirar em inimigos vermelhos que aparecem nas bordas da tela
- **Inimigos**: Patrulham aleatoriamente, mudam de direção a cada 3 segundos
- **Colisão**: Flechas eliminam inimigos ao colidir
- **UI**: Mostra estatísticas em tempo real (flechas disparadas, inimigos mortos, zoom atual)
- **Dificuldade**: Aumenta gradualmente com spawn contínuo de inimigos

## Padrões de Design Implementados

### 🎮 InputProcessor Pattern
- **GameInputProcessor.java**: Implementa `InputProcessor` interface com flags booleanas
- Centraliza toda lógica de input do jogo
- Facilita testes e reutilização
- Evita chamadas diretas ao `Gdx.input`

### 📦 Asset Manager Pattern
- **AssetManager**: Gerencia centralizado de recursos (áudio, imagens, fonts)
- Carregamento assíncrono de assets
- Cleanup automático na disposição
- Previne vazamento de memória

### ♻️ Object Pooling Pattern
- **ObjectPool.java**: Classe abstrata genérica para reutilização de objetos
- **ArrowPool.java**: Pool especializado para Arrows
- **EnemyPool.java**: Pool especializado para Enemies
- Reduz garbage collection, melhora frame rate
- Reutiliza instâncias em vez de criar/destruir continuamente

## Arquivos Criados

```
core/src/main/java/io/github/some_example_name/
├── Main.java              # Classe principal (Game)
├── GameTimer.java         # Sistema de timer reutilizável (delta-based)
├── GameInputProcessor.java # InputProcessor para input centralizado
├── ObjectPool.java        # Abstract class para object pooling genérico
├── ArrowPool.java         # Pool especializado para Arrows
├── EnemyPool.java         # Pool especializado para Enemies
├── Arrow.java             # Entidade de flecha com som e pooling
├── Archer.java            # Entidade de arqueiro com cooldown
├── Enemy.java             # Entidade inimiga com AI e pooling
└── Explosion.java         # Efeito visual (auxiliar)

assets/sounds/
├── shoot.wav              # Som de disparo (gerado)
└── background.wav         # Música de background (gerado)
```

## Compilação e Execução

### Opção 1: Gradle (Recomendado)

```bash
# Limpar e compilar
./gradlew clean build

# Executar LWJGL3
./gradlew lwjgl3:run
```

### Opção 2: IDE (VS Code/IntelliJ)

Importe o projeto como Gradle project e execute a tarefa `lwjgl3:run`

## Asset Manager - Carregamento de Recursos

O projeto usa `AssetManager` para gerenciar recursos:

```java
// Assets carregados em create()
assetManager = new AssetManager();
assetManager.load("sounds/shoot.wav", Sound.class);
assetManager.load("sounds/background.wav", Music.class);
assetManager.finishLoading();

// Cleanup automático em dispose()
assetManager.dispose();
```

**Formatos suportados**: WAV, OGG, MP3

**Se os arquivos não existirem:**
- O jogo funciona normalmente (assets são opcionais)
- Logs de erro são exibidos mas não interrompem execução
- Try-catch garante robustez

**Para adicionar sons:**

1. Coloque seus arquivos na pasta `assets/sounds/`
2. Atualize os nomes em `Main.java` create() se necessário
3. O AssetManager carregará automaticamente

**Fontes recomendadas para sons:**
- [Freesound.org](https://freesound.org)
- [OpenGameArt.org](https://opengameart.org)
- [Zapsplat](https://www.zapsplat.com)

## Explicação Técnica

### Timer Delta-Based
```java
// Em GameTimer.java
public void update(float delta) {
    if (!finished) {
        elapsed += delta;
        if (elapsed >= duration) {
            finished = true;
        }
    }
}
```

### Camera Project/Unproject
```java
// Converter posição do mouse para coordenadas do mundo
touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(touchPoint);  // Tela → Mundo
// camera.project(vector);      // Mundo → Tela
```

### Som Único por Entidade
```java
// Arrow.java - cada flecha tem sua própria instância de som
public class Arrow {
    private Sound shootSound;
    
    public void update(float delta) {
        if (!soundPlayed && shootSound != null) {
            shootSound.play();
            soundPlayed = true;  // Toca apenas uma vez
        }
    }
}
```

## Critérios de Avaliação Atendidos

- ✅ **Sound**: Implementado em contexto OO (Arrow.java)
- ✅ **Música**: Background music com looping
- ✅ **Câmera**: Translação + Zoom + Project/Unproject
- ✅ **Timer**: Delta-based, OO, uso em animação e eventos
- ✅ **Baixo Acoplamento**: Cada classe responsável por si

## Detalhes Técnicos - Arquitetura

### InputProcessor Pattern
```java
// Em Main.create()
inputProcessor = new GameInputProcessor();
Gdx.input.setInputProcessor(inputProcessor);

// Em processInput()
if (inputProcessor.wPressed) {
    cameraTarget.y += speed;
}
```

### Object Pooling Pattern
```java
// Criação de pools
arrowPool = new ArrowPool(shootSound, 10, 50);
enemyPool = new EnemyPool(5, 30);

// Obtenção de objeto do pool
Arrow arrow = arrowPool.obtain(x, y, angle);

// Devolução ao pool
arrowPool.free(arrow);
```

### AssetManager Pattern
```java
// Carregamento centralizado
assetManager.load("sounds/shoot.wav", Sound.class);
sound = assetManager.get("sounds/shoot.wav", Sound.class);

// Cleanup único
assetManager.dispose();  // Libera todos os assets
```

## Possíveis Extensões

Para tornar o projeto mais completo:
- Implementar sistema de pontuação com persistência
- Adicionar power-ups (velocidade, munição infinita)
- Criar diferentes tipos de inimigos com comportamentos variados
- Implementar fases/níveis com dificuldade crescente
- Adicionar efeitos de partículas para explosões
- Implementar som de colisão/morte do inimigo
- Criar menu principal e tela de game over
- Adicionar jogabilidade multijogador local
