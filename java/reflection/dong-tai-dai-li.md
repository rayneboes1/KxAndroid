# JDK 动态代理

运行时生成代理类class

```text
// 定义接口
interface GithubService {
    void getUser();

    void getRepos(String username);
}

interface TestService{
    void test();
}
```

```text
GithubService p = (GithubService) Proxy.newProxyInstance(GithubService.class.getClassLoader(), new Class[]{GithubService.class,TestService.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("执行方法：" + method.getName() + ",对象：" + proxy.getClass().getName()+",参数："+ Arrays.toString(args));
                return null;
            }
        });
```

## 生成的代理类

通过将对方法的调用转发给 InvocationHandler，实现方法的代理。

```text
final class $Proxy0 extends Proxy implements GithubService, TestService {
    private static Method m1;
    private static Method m4;
    private static Method m5;
    private static Method m2;
    private static Method m3;
    private static Method m0;

    public $Proxy0(InvocationHandler var1) throws  {
        super(var1);
    }

    public final boolean equals(Object var1) throws  {
        try {
            return (Boolean)super.h.invoke(this, m1, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final void getUser() throws  {
        try {
            super.h.invoke(this, m4, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final void test() throws  {
        try {
            super.h.invoke(this, m5, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final String toString() throws  {
        try {
            return (String)super.h.invoke(this, m2, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    public final void getRepos(String var1) throws  {
        try {
            super.h.invoke(this, m3, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final int hashCode() throws  {
        try {
            return (Integer)super.h.invoke(this, m0, (Object[])null);
        } catch (RuntimeException | Error var2) {
            throw var2;
        } catch (Throwable var3) {
            throw new UndeclaredThrowableException(var3);
        }
    }

    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m4 = Class.forName("GithubService").getMethod("getUser");
            m5 = Class.forName("TestService").getMethod("test");
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("GithubService").getMethod("getRepos", Class.forName("java.lang.String"));
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
}
```

[Java 动态代理](https://juejin.im/post/5ad3e6b36fb9a028ba1fee6a)







