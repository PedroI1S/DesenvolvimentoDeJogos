package io.github.some_example_name.pools;

import java.util.ArrayList;
import java.util.List;

/**
 * Object Pool genérico para reutilização de objetos
 * Reduz garbage collection e melhora performance
 */
public abstract class ObjectPool<T> {
    private List<T> available;
    private List<T> inUse;
    private int maxSize;

    public ObjectPool(int initialSize, int maxSize) {
        this.available = new ArrayList<>(initialSize);
        this.inUse = new ArrayList<>();
        this.maxSize = maxSize;
        
        // Cria objetos iniciais
        for (int i = 0; i < initialSize; i++) {
            available.add(createNew());
        }
    }

    /**
     * Método abstrato para criar novo objeto
     * Implementado pelas subclasses
     */
    protected abstract T createNew();

    /**
     * Obtém um objeto do pool
     * Se não houver disponível, cria novo (até maxSize)
     */
    public T obtain() {
        T obj;
        
        if (!available.isEmpty()) {
            obj = available.remove(available.size() - 1);
        } else if (inUse.size() < maxSize) {
            obj = createNew();
        } else {
            // Pool está cheio, retorna nulo ou reutiliza o mais antigo
            obj = inUse.remove(0);
            reset(obj);
        }
        
        inUse.add(obj);
        return obj;
    }

    /**
     * Devolve um objeto para o pool
     */
    public void free(T obj) {
        if (inUse.remove(obj)) {
            reset(obj);
            available.add(obj);
        }
    }

    /**
     * Reseta o objeto para seu estado inicial
     * Implementado pelas subclasses
     */
    protected abstract void reset(T obj);

    /**
     * Retorna lista de objetos em uso
     */
    public List<T> getInUse() {
        return inUse;
    }

    public int getInUseCount() {
        return inUse.size();
    }

    public int getAvailableCount() {
        return available.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Informações do pool
     */
    public String getStats() {
        return String.format("Pool: %d in use, %d available, max %d", 
            inUse.size(), available.size(), maxSize);
    }
}
