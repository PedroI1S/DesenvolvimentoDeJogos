/**
 * EXEMPLOS DE USO - Bow and Arrow Project
 * 
 * Este arquivo demonstra como usar as classes criadas
 * e os conceitos-chave implementados
 */

// ============================================
// 1. USANDO GameTimer
// ============================================

// Exemplo 1: Timer simples para contagem
GameTimer countdownTimer = new GameTimer(5f);  // 5 segundos

void update(float delta) {
    countdownTimer.update(delta);
    
    if (countdownTimer.isFinished()) {
        System.out.println("5 segundos passaram!");
    }
    
    System.out.println("Progresso: " + countdownTimer.getProgress()); // 0.0 a 1.0
}

// Exemplo 2: Animação com timer
GameTimer animTimer = new GameTimer(1f);  // 1 segundo

void render() {
    animTimer.update(delta);
    
    // Aumenta escala de 0 a 1 durante 1 segundo
    float scale = animTimer.getProgress();
    shapeRenderer.circle(x, y, radius * scale);
    
    // Reset automático
    if (animTimer.isFinished()) {
        animTimer.reset();
    }
}

// ============================================
// 2. USANDO Arrow
// ============================================

// Criar flecha
Sound shootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.wav"));
Arrow arrow = new Arrow(archerX, archerY, angle, shootSound);

// Adicionar à lista
List<Arrow> arrows = new ArrayList<>();
arrows.add(arrow);

// Atualizar
void update(float delta) {
    for (Arrow arrow : arrows) {
        arrow.update(delta);
    }
    
    // Remover flechas mortas
    arrows.removeIf(a -> !a.isAlive());
}

// Renderizar
void render() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (Arrow arrow : arrows) {
        arrow.render(shapeRenderer);
    }
    shapeRenderer.end();
}

// ============================================
// 3. USANDO Archer
// ============================================

// Criar arqueiro
Sound shootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.wav"));
Archer archer = new Archer(x, y, shootSound);

// Atualizar (aponta para alvo)
void update(float delta) {
    archer.update(delta, mouseX, mouseY);
}

// Disparar
void onMouseClick() {
    if (archer.canShoot()) {
        Arrow newArrow = archer.shoot();
        if (newArrow != null) {
            arrows.add(newArrow);
        }
    }
}

// Renderizar
void render() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    archer.render(shapeRenderer);
    shapeRenderer.end();
}

// ============================================
// 4. CAMERA COM PROJECT/UNPROJECT
// ============================================

// Inicializar câmera
OrthographicCamera camera = new OrthographicCamera();
camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);

// Converter coordenadas tela → mundo
Vector3 screenCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
camera.unproject(screenCoords);  // Agora screenCoords contém coords do mundo
float worldX = screenCoords.x;
float worldY = screenCoords.y;

// Converter coordenadas mundo → tela
Vector3 worldCoords = new Vector3(objectX, objectY, 0);
camera.project(worldCoords);  // Agora tem coords da tela
float screenX = worldCoords.x;
float screenY = worldCoords.y;

// Usar para verificar se objeto está na tela
if (worldCoords.x >= 0 && worldCoords.x <= Gdx.graphics.getWidth() &&
    worldCoords.y >= 0 && worldCoords.y <= Gdx.graphics.getHeight()) {
    // Renderizar (está visível)
}

// ============================================
// 5. CAMERA COM ZOOM E TRANSLAÇÃO
// ============================================

// Controlar zoom (1.0 = zoom 100%, 2.0 = zoom 200%)
float currentZoom = 1f;
float MIN_ZOOM = 0.5f;
float MAX_ZOOM = 3f;

// Aumentar zoom
currentZoom = Math.min(currentZoom + 0.1f, MAX_ZOOM);
camera.zoom = 1f / currentZoom;  // Inverter: zoom 2.0 = visualizar 0.5x

// Diminuir zoom
currentZoom = Math.max(currentZoom - 0.1f, MIN_ZOOM);
camera.zoom = 1f / currentZoom;

// Mover câmera
Vector2 cameraPos = new Vector2(camera.position.x, camera.position.y);
cameraPos.x += velocityX * delta;
cameraPos.y += velocityY * delta;

// Garantir que não sai do mundo
float halfWidth = WORLD_WIDTH / (2f * currentZoom);
float halfHeight = WORLD_HEIGHT / (2f * currentZoom);
cameraPos.x = Math.max(halfWidth, Math.min(cameraPos.x, WORLD_WIDTH - halfWidth));
cameraPos.y = Math.max(halfHeight, Math.min(cameraPos.y, WORLD_HEIGHT - halfHeight));

camera.position.set(cameraPos.x, cameraPos.y, 0);
camera.update();

// ============================================
// 6. ÁUDIO - SOM ÚNICO POR ENTIDADE
// ============================================

public class Explosion {
    private Sound explosionSound;
    
    public Explosion(float x, float y, Sound sound) {
        // Recebe referência ao som via construtor
        this.explosionSound = sound;
    }
    
    public void trigger() {
        if (explosionSound != null) {
            // Toca som UMA ÚNICA VEZ
            explosionSound.play();
        }
    }
}

// Uso:
Sound explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/boom.wav"));
Explosion explosion = new Explosion(100, 100, explosionSound);
explosion.trigger();  // Toca som

// ============================================
// 7. DELTA TIME - FRAME-RATE INDEPENDENTE
// ============================================

// ❌ ERRADO: Hardcoded velocidade (depende do FPS)
void update() {
    x += 100;  // Sempre 100 unidades por frame
}

// ✅ CORRETO: Usa delta time
void update(float delta) {
    x += 100 * delta;  // 100 unidades POR SEGUNDO
    // Se delta = 0.016 (60 FPS): x += 1.6
    // Se delta = 0.033 (30 FPS): x += 3.3
    // Mesma velocidade em ambos os casos!
}

// ============================================
// 8. ORIENTAÇÃO A OBJETO - RESPONSABILIDADES
// ============================================

// GameTimer
// Responsabilidade: Contar tempo
// Não sabe: O que está sendo cronometrado

// Arrow
// Responsabilidade: Renderizar flecha, tocar som, se mover
// Não sabe: Quem criou, como é gerenciado

// Archer
// Responsabilidade: Apontar, disparar, controlar cooldown
// Não sabe: O que acontece com as flechas depois

// Main
// Responsabilidade: Orquestrar tudo, processar input
// Não sabe: Detalhes internos de cada classe

// Resultado: Baixo acoplamento, alta coesão!

// ============================================
// 9. EXEMPLO COMPLETO - PEQUENININHO
// ============================================

public class SimpleGame extends ApplicationAdapter {
    
    private Archer archer;
    private List<Arrow> arrows = new ArrayList<>();
    private Sound shootSound;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    @Override
    public void create() {
        // Setup básico
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        shootSound = Gdx.audio.newSound(Gdx.files.internal("sounds/shoot.wav"));
        
        // Criar arqueiro
        archer = new Archer(512, 384, shootSound);
    }
    
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        // Update
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        
        archer.update(delta, mouse.x, mouse.y);
        
        for (Arrow arrow : arrows) {
            arrow.update(delta);
        }
        arrows.removeIf(a -> !a.isAlive());
        
        // Input
        if (Gdx.input.justTouched()) {
            Arrow newArrow = archer.shoot();
            if (newArrow != null) {
                arrows.add(newArrow);
            }
        }
        
        // Render
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        archer.render(shapeRenderer);
        for (Arrow arrow : arrows) {
            arrow.render(shapeRenderer);
        }
        shapeRenderer.end();
    }
}

// ============================================
// CONCEITOS CHAVE DEMONSTRADOS
// ============================================

/*
1. ENCAPSULAMENTO:
   - Cada classe gerencia seu próprio estado
   - Arrow não expõe Sound diretamente
   - GameTimer não sabe o que está cronometrando

2. REUSABILIDADE:
   - GameTimer pode ser usado para qualquer coisa
   - Archer pode disparar quantas Arrow quiser
   - Arrow pode ser usada em qualquer jogo

3. DELTA TIME:
   - Garante mesma velocidade em qualquer FPS
   - Multiplicar tudo por delta: x += velocity * delta

4. PROJECT/UNPROJECT:
   - Converter entre sistemas de coordenadas
   - unproject: Tela → Mundo
   - project: Mundo → Tela

5. CAMERA AVANÇADA:
   - Zoom: camera.zoom = 1f / zoomLevel
   - Translação: camera.position += delta * velocity
   - Bounds: Garante não sai do mundo

6. ÁUDIO EFICIENTE:
   - Uma instância por tipo de som
   - Classes que usam som recebem via construtor
   - Sem carregamento desnecessário

7. BAIXO ACOPLAMENTO:
   - Classes não conhecem umas às outras
   - Apenas Main orquestra tudo
   - Fácil adicionar/remover/modificar classes
*/
