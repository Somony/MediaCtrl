## 实现的功能

- photoselect 实现对跳转到系统相机、相册选取图片之后剪切的封装

- permission 实现对android6.0动态权限的封装，适用于原生系统，国内系统还需进一步适配

- camera 对自定义相机的封装

    - 基本功能差不多完善，因为surfaceView是在放置在屏幕后面的，所以可以自定义拍照框等效果。后续添加拍照预览时的自定义框等，还有只取局部图像的方式
    - 自定义相机录像上还需要优化size和视频质量

- audio 对AudioRecord进行录音的封装

  实现功能：

  1. 录音pcm格式或者wav格式

  2. 录音开始，暂停和继续，结束，保存

  3. 录音期间可以保存指定时长的片段录音文件

  4. 对完整录音文件的操作回调

     ​
  其中：

  录音文件16k采样率，单通道，16位，需修改请找到注释的地方

  若指定需要保存片段文件，那么保存片段文件，其中片段文件和完整文件格式应该一样

  暂存的问题:

  录音期间出现buffer overflow

  详细描述：在开始录音后，会一直出现buffer overflow的警告，但是不会出问题，暂停或不在读取audio中的数据，再开始不会有问题。但是暂停了然后直接结束，就会在开始的时候给出buffer overflow警告，之后线程就会卡死在开始，获得不到录音数据。

  解决：

  stack overflow上回答说是构造audioRecord实例时的minBufferSize过小，官方文档也说AudioRecord.getMinBufferSize对于构造AudioRecord小点，需要自己扩大，所以在构造的时候将minBuffer*10，之后在录音期间不再出现buffer overflow问题，但是在暂停时还是会出现。<br>

   在暂停了再结束后，再次点击开始，会将上次暂停还存在再AudioRecord中的数据写入本地，之后就不再接受录音数据了，而且也会出现buffer overflow警告，设想应该是上次暂停后的数据没有读取完毕，导致buffer overflow，而导致后续出错。所以判断状态是暂停了再结束就将剩余的数据也写入到本地，但是这样还是不行，重复这个问题，还待解决。


   - 整个工具类必须需要在Manifest中配置的项

     ```xml
     <activity
             android:name=".utils.permission.PermissionActivity"
             android:theme="@style/GrantNoDisplay"/>

     <provider
         android:name="android.support.v4.content.FileProvider"
         android:authorities="com.example.originaltec.fileprovider"
         android:exported="false"
         android:grantUriPermissions="true">
         <meta-data
             android:name="android.support.FILE_PROVIDER_PATHS"
             android:resource="@xml/file_paths"/>
     </provider>

     <activity android:name=".utils.photoselect.PhotoSelectActivity"/>
     ```