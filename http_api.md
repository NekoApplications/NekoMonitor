# HTTP API

查询操作的请求头需要带`Auth-Method: {验证方法}`

验证方法的取值为
 - `infoUpload`(用于探针上传系统数据) 
 - `dataQuery`(用于查询系统数据信息) 
 - `debug`(仅当环境变量中nm.env=DEVELOPMENT时，即当前模式为开发模式时启用)

对于`dataQuery`验证方式，要求请求头`Access-Key`为访问密钥  

示例curl命令如下：

从时间`a`到时间`b`获取来自服务器`agent`的最多`n`个系统数据：  
时间`a`与`b`都为unix时间戳格式
```shell
curl -v -H "AuthMethod: dataQuery\nAccess-Key: your-access-key" http://localhost:{port}/query/status/{agent}?fromTime={a}&toTime={b}&countLimit={n}
```
响应（200）：
```json
{
  "result":"SUCCESS",
  "message":"",
  "data":[
    //数量不超过n条的来自探针的完整数据
  ],
  "compressedData":""
}
```

响应（401）验证失败：
```json
{
  "result":"AUTH_FAILED", 
  "message":"",
  "data":[],
  "compressedData":""
}
```
响应（400）请求参数有误：
```json
{
  "result":"FAILURE",
  "message":"错误信息",
  "data":[],
  "compressedData":""
}
```

获取所有探针的最新系统数据：
```shell
curl -v -H "AuthMethod: dataQuery\nAccess-Key: your-access-key" http://localhost:14900/query/all
```
响应（200）：
```json
{
  "result":"SUCCESS",
  "message":"",
  "data":{
    "探针名称":{
      //数据
    }
    //以此类推
  },
  "compressedData":""
}
```

响应（401）验证失败：
```json
{
  "result":"AUTH_FAILED", 
  "message":"",
  "data":[],
  "compressedData":""
}
```


