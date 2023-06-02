package bearmaps.proj2ab;

import java.util.*;


public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T>{
    private final TreeMap<Double, Set<T>> priorityToItem = new TreeMap<>();
    private final HashMap<T, Double> itemToPriority = new HashMap<>();

    /**
     * 由于同个权值下可能有多个项目，因此我们使用iterator返回集合中的第一个项目
     * @param s 集合
     * @return 所有项目
     */
    private static <K> K getItem(Set<K> s) {
        Iterator<K> i = s.iterator();
        return i.next();
    }

    /**
     * 添加带有权值的项目
     * @param item 待添加的项目
     * @param priority 项目的权值
     */
    @Override
    public void add(T item, double priority) {
        if (itemToPriority.containsKey(item)) {
            throw new IllegalArgumentException("已经存在" + item);
        }
        if (!priorityToItem.containsKey(priority)) {
            priorityToItem.put(priority, new HashSet<T>());
        }
        Set<T> itemsWithPriority = priorityToItem.get(priority);
        itemsWithPriority.add(item);
        itemToPriority.put(item, priority);
    }

    /**
     * 查询PQ中是否已经存在item
     * @param item 需要查询的项目
     * @return 直接使用containsKey查询的结果
     */
    @Override
    public boolean contains(T item) {
        return itemToPriority.containsKey(item);
    }

    /**
     * TreeMap默认按照Key的大小来排序（升序），因此想要获得权值最小的项目，只需要找到第一个Key。
     * @return 返回具有最小的权值的项目
     */
    @Override
    public T getSmallest() {
        if (itemToPriority.size() == 0) {
            throw new NoSuchElementException("队列为空");
        }
        Set<T> itemsWithLowestPriority = priorityToItem.get(priorityToItem.firstKey());
        return getItem(itemsWithLowestPriority);
    }

    /**
     * 删除并返回权值最小的项目。
     * @return 权值最小的项目
     */
    @Override
    public T removeSmallest() {
        T item = getSmallest();
        double lowestPriority = itemToPriority.get(item);
        itemToPriority.remove(item);
        priorityToItem.get(lowestPriority).remove(item);
        Set<T> itemWithLowestPriority = priorityToItem.get(lowestPriority);
        if(itemWithLowestPriority.size()==0)
            priorityToItem.remove(lowestPriority);
        return item;
    }

    /**
     * 修改指定项目的权值
     * @param item 一个项目
     * @param priority 修改后的权值
     */
    @Override
    public void changePriority(T item, double priority) {
        if (!contains(item)) {
            throw new IllegalArgumentException(item + " not in PQ.");
        }

        double oldP = itemToPriority.get(item);
        Set<T> itemsWithOldPriority = priorityToItem.get(oldP);
        itemsWithOldPriority.remove(item);

        if (itemsWithOldPriority.size() == 0) {
            priorityToItem.remove(oldP);
        }

        itemToPriority.remove(item);
        add(item, priority);
    }

    /**
     * 返回队列的大小
     * @return 队列的大小
     */
    @Override
    public int size() {
        return itemToPriority.size();
    }
}
