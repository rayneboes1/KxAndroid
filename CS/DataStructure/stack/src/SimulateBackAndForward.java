/**
 * 模拟浏览器后退前进
 * 思路：用两个栈实现
 */
public class SimulateBackAndForward {
    Stack back;
    Stack forward;


    SimulateBackAndForward(int maxPage) {
        back = new ArrayStack(maxPage);
        forward = new LinkedStack(maxPage);
    }

    public boolean view(int page) {
        forward.clear();
        return back.push(page);
    }

    public int back() {
        //只剩一页无法后退
        if (back.size() == 1) {
            return -1;
        }
        int page = back.pop();
        forward.push(page);
        return page;
    }

    public int forward() {
        int page = forward.pop();
        if (page != -1) {
            back.push(page);
        }
        return page;
    }

}
