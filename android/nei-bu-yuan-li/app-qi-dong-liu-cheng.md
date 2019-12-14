# App 启动流程

1. Launcher startActivity
2. AMS startActivity
3. Zygote fork进程
4. Activity main\(\)
5. ActivityThread 进程loop循环
6. 开启Activity,开始生命周期回调…

