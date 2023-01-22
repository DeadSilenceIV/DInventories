package us.lynuxcraft.deadsilenceiv.dutilities.inventory;

import lombok.Getter;
import org.bukkit.entity.HumanEntity;
import us.lynuxcraft.deadsilenceiv.dutilities.Pair;

import java.util.*;

public abstract class MultiPagesInventory<T extends InventoryPage>{
    protected UUID uuid;
    @Getter protected int size;
    @Getter protected final Map<Integer,T> pages;
    protected Integer cachedHashCode;
    public MultiPagesInventory(){
        uuid = UUID.randomUUID();
        pages = new HashMap<>();
    }

    public void openPage(HumanEntity entity, int id){
        getPageById(id).open(entity);
    }

    protected void createPages(int size){
        int numberOfPages = (int) Math.floor((double) size/ (double) 45);
        int totalRows = (size/9);
        if(size>45) {
            int pagesToCreate = (size % 45 == 0) ? (numberOfPages-1) : numberOfPages;
            int lastPageSlots = (size % 45 != 0) ? (totalRows-(numberOfPages*5))*9 : 45;
            for (int i = 0; i <= pagesToCreate; i++){
                pages.put(i,i < pagesToCreate ? newPage(i, 45) : newPage(i, lastPageSlots));
            }
        }else{
            pages.put(0,newPage(0, size));
        }
        this.size = size;
    }

    protected Pair<ResizeResult, Set<T>> resize(int newSize){
        int numberOfPages = (int) Math.floor((double) newSize/ (double) 45);
        int totalRows = (newSize/9);
        int pagesToCreate = (newSize % 45 == 0) ? (numberOfPages-1) : numberOfPages;
        int lastPageSlots = (newSize % 45 != 0) ? (totalRows-(numberOfPages*5))*9 : 45;
        ResizeResult result;
        Set<T> modified = new HashSet<>();
        if(numberOfPages > pages.size()){
            result = ResizeResult.ADDED_PAGES;
            for (int i = pages.size()-1; i < numberOfPages; i++) {
                T added = pages.put(i,i < pagesToCreate ? newPage(i, 45) : newPage(i, lastPageSlots));
                modified.add(added);
            }
        }else if(numberOfPages < pages.size()){
            result = ResizeResult.REMOVED_PAGES;
            for (int i = numberOfPages-1; i < pages.size(); i++) {
                T removed = pages.remove(i);
                modified.add(removed);
            }
            changeLastPageSlots(lastPageSlots);
        }else{
            result = ResizeResult.NOTHING;
            changeLastPageSlots(lastPageSlots);
        }
        this.size = newSize;
        return new Pair<>(result,modified);
    }

    protected void changeLastPageSlots(int newAmount){
        int lastPage = pages.size()-1;
        int currentLastPageSlots = pages.get(lastPage).getBukkitInventory().getSize();
        if(currentLastPageSlots != newAmount){
            pages.remove(lastPage);
            pages.put(pages.size(),newPage(pages.size(), newAmount));
        }
    }

    protected void loadPages(){
        for(T page : getPages().values()){
            page.setupPage();
        }
    }

    protected abstract T newPage(int id,int size);

    public T getPageById(int id){
        return getPages().get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiPagesInventory)) return false;
        MultiPagesInventory<?> that = (MultiPagesInventory<?>) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        if(cachedHashCode == null) {
            cachedHashCode =  Objects.hash(uuid);
        }
        return cachedHashCode;
    }

    protected enum ResizeResult{
        ADDED_PAGES,
        REMOVED_PAGES,
        NOTHING
    }
}
