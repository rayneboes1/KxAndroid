public interface Queue {

    boolean enqueue(int data);

    int deque();

    boolean isEmpty();

    int size();

    void clear();
}
