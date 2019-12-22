/**
 * 简单的正则表达式匹配，包含*和?两个通配符
 * 其中*匹配任意多个字符,?匹配一个或零个字符
 */
public class MatchPattern {
    private boolean match = false;

    public boolean match(String pattern, String text) {
        match = false;
        matchInternal(pattern, text, 0, 0);
        return match;
    }

    private void matchInternal(String pattern, String text, int pi, int ti) {
        //匹配成功后不再匹配
        if (match) {
            return;
        }
        //表达式到达结尾
        if (pi == pattern.length()) {
            //表达式和文本都到达末尾，则说明匹配
            if (ti == text.length()) {
                match = true;
            }
            return;
        }
        char pc = pattern.charAt(pi);
        if (pc == '*') {
            for (int i = ti; i <= text.length(); i++) {
                matchInternal(pattern, text, pi + 1, i);
            }
        } else if (pc == '?') {
            matchInternal(pattern, text, pi + 1, ti);
            matchInternal(pattern, text, pi + 1, ti + 1);
        } else if (ti < text.length() && pc == text.charAt(ti)) {
            matchInternal(pattern, text, pi + 1, ti + 1);
        }
    }


    public static void main(String[] args) {
        MatchPattern matchPattern = new MatchPattern();
        boolean result = matchPattern.match("abc*", "abcddd");
        System.out.println("是否匹配：" + result);
    }

}
