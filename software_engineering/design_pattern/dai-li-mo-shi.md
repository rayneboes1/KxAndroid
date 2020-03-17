---
description: Proxy Pattern
---

# 代理模式

## 定义

在代理模式（Proxy Pattern）中，一个类代表另一个类的功能。这种类型的设计模式属于结构型模式。在代理模式中，我们创建具有现有对象的对象，以便向外界提供功能接口。

* 意图：为其他对象提供一种代理以控制对这个对象的访问。
* 主要解决：在直接访问对象时带来的问题，比如说：要访问的对象在远程的机器上。在面向对象系统中，有些对象由于某些原因（比如对象创建开销很大，或者某些操作需要安全控制，或者需要进程外的访问），直接访问会给使用者或者系统结构带来很多麻烦，我们可以在访问此对象时加上一个对此对象的访问层。
* 何时使用：想在访问一个类时做一些控制。
* 如何解决：增加中间层。
* 优点： 1、职责清晰。 2、高扩展性。 3、智能化。
* 缺点： 1、由于在客户端和真实主题之间增加了代理对象，因此有些类型的代理模式可能会造成请求的处理速度变慢。 2、实现代理模式需要额外的工作，有些代理模式的实现非常复杂。
* 使用场景：按职责来划分，通常有以下使用场景： 1、远程代理。 2、虚拟代理。 3、Copy-on-Write 代理。 4、保护（Protect or Access）代理。 5、Cache代理。 6、防火墙（Firewall）代理。 7、同步化（Synchronization）代理。 8、智能引用（Smart Reference）代理。
* 注意事项： 
  * 和适配器模式的区别：适配器模式主要改变所考虑对象的接口，而代理模式不能改变所代理类的接口。 
  * 和装饰器模式的区别：装饰器模式为了增强功能，而代理模式是为了加以控制。

## 分类

代理模式根据其目的和实现方式不同可分为很多种类，其中常用的几种代理模式简要说明如下：

* 远程代理\(Remote Proxy\)：给一个位于不同的地址空间的对象提供一个本地的代理对象，这个不同的地址空间可以是在同一台主机中，也可是在另一台主机中，远程代理又称为大使\(Ambassador\)。
* 虚拟代理\(Virtual Proxy\)：如果需要创建一个资源消耗较大的对象，先创建一个消耗相对较小的对象来表示，真实对象只在需要时才会被真正创建。
* 保护代理\(Protect Proxy\)：控制对一个对象的访问，可以给不同的用户提供不同级别的使用权限。
* 缓冲代理\(Cache Proxy\)：为某一个目标操作的结果提供临时的存储空间，以便多个客户端可以共享这些结果。
* 智能引用代理\(Smart Reference Proxy\)：当一个对象被引用时，提供一些额外的操作，例如将对象被调用的次数记录下来等

## 实例

![UML &#x7C7B;&#x56FE;](../../.gitbook/assets/image%20%2853%29.png)

```text
public interface Image {
   void display();
}
```

```text
public class RealImage implements Image {
 
   private String fileName;
 
   public RealImage(String fileName){
      this.fileName = fileName;
      loadFromDisk(fileName);
   }
 
   @Override
   public void display() {
      System.out.println("Displaying " + fileName);
   }
 
   private void loadFromDisk(String fileName){
      System.out.println("Loading " + fileName);
   }
}
```



```text
public class ProxyImage implements Image{
 
   private RealImage realImage;
   private String fileName;
 
   public ProxyImage(String fileName){
      this.fileName = fileName;
   }
 
   @Override
   public void display() {
      if(realImage == null){
         realImage = new RealImage(fileName);
      }
      realImage.display();
   }
}
```



```text
public class ProxyPatternDemo {
   
   public static void main(String[] args) {
      Image image = new ProxyImage("test_10mb.jpg");
 
      // 图像将从磁盘加载
      image.display(); 
      System.out.println("");
      // 图像不需要从磁盘加载
      image.display();  
   }
}
```

## 相关链接

[代理模式\|菜鸟教程](https://www.runoob.com/design-pattern/proxy-pattern.html)

