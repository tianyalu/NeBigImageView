#### NeBigImageView 大长图加载

* 如果Android直接用ImageView加载大长图时会报`java.lang.RuntimeException: Canvas: trying to draw too large(162518400bytes) bitmap`异常
* 本文采用分块加载和内存复用等方式实现了大长图的加载

###### 示例如下图所示：

![image](https://github.com/tianyalu/NeBigImageView/blob/master/screenshot/show.gif)

