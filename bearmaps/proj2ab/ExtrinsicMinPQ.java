package bearmaps.proj2ab;
/**
 * 优先级队列中的对象有一个外部提供的权值，即在插入时作为参数提供，并可以使用changePriority改变。
 */
public interface ExtrinsicMinPQ<T> {
    /* Inserts an item with the given priority value. */
    void add(T item, double priority);
    /* Returns true if the PQ contains the given item. */
    boolean contains(T item);
    /* Returns the minimum item. */
    T getSmallest();
    /* Removes and returns the minimum item. */
    T removeSmallest();
    /* Changes the priority of the given item. Behavior undefined if the item doesn't exist. */
    void changePriority(T item, double priority);
    /* Returns the number of items in the PQ. */
    int size();
}
