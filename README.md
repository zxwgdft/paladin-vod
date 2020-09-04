# paladin-vod
简单的视频上传、转码、点播系统和解决方案，用户上传视频后，使用ffmepg对视频进行转码，然后使用nginx和nginx rtmp模块提供视频点播功能

## 功能介绍
1. 视频上传，利用文件块上传来支持续传（这里并没有计算文件md5验证，并且为了简化逻辑，不支持并发上传块文件，效率不高）
2. 视频转码，利用ffmepg对视频进行转码（转成aac编码的音频和h264编码的视频），同时视频大小将被大大压缩（需要安装ffmepg）
3. 视频点播，搭建加载nginx-rtmp-module模块的nginx，进行点播（安装带rtmp模块的nginx）

## 项目运行
创建数据库，并执行项目中的vod.sql文件创建表，然后修改application-dev.yml中的数据库配置后即可运行

## 安装nginx和nginx-rtmp-module
网上介绍安装的很多，这里只简单介绍我这边的安装，环境为centos。


1. 下载nginx
```
git clone https://github.com/arut/nginx-rtmp-module.git
wget http://nginx.org/download/nginx-1.8.1.tar.gz
tar -zxvf nginx-1.8.1.tar.gz
```

2. 安装编译环境
```
yum -y install gcc-c++
yum -y install pcre-devel openssl openssl-devel
```
3. 开启MP4模块,注意: 一定要开启ssl模块
```
./configure --prefix=/usr/local/nginx --with-http_ssl_module --with-http_mp4_module
make && sudo make install
```
4. 配置rtmp
```
./configure --add-module=/home/focus/live_streaming/nginx-rtmp-module
make && sudo make install
```
5. 配置h264
```
./configure --add-module=/home/focus/live_streaming/nginx_mod_h264_streaming-2.2.7
make && sudo make install
```

## 不出意外的话, make会报两个error需要处理
在文件: h264源码目录/src/ngx_http_streaming_module.c
将如下部分注释掉:
```
/* TODO: Win32 */
if (r->zero_in_uri)
{
 return NGX_DECLINED;
}
```
在文件: nginx源码目录/objs/Makefile中删除参数 -Werror


## nginx配置设置
```
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}

# 点播配置
rtmp {
    server{
    	listen 1935;
    	chunk_size 4096;
    	
    	application vod{
            # 转码后视频存放的文件夹
            play /usr/local/nginx/video;
        }
    }
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       9999;
        server_name  video_server;
        # 指向了视频存放的根目录
        root /usr/local/nginx/video;
        limit_rate 512k;

        #charset koi8-r;
        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
        }

    	location ~ \.mp4$ {
    		mp4;
    	}

    	#location ~ \.flv$ { 
    	#	flv;
    	#}

        # error_page  404              /404.html;
        # redirect server error pages to the static page /50x.html
        error_page	500 502 503 504	/50x.html;
    	location = /50x.html {
            root   html;
        }     
    }
}
```
