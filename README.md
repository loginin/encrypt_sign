# 功能说明
EncryptMain方法实现了对请求签名, 对响应验签的过程.

# 签名方法
    1. 根据秘钥key对data字段进行默认的AES/ECB/PKCS5Padding加密,并 Base64 编码(非 base64Url)
    2. 签名
    2.1. 排序:将所有待传参数名按照字典序,包括code和msg字段(若有)
    2.2. 拼接:将这些参数[参数名=参数值]按顺序用&符号拼接起来，最后把key 追加到尾部，如 a=1&b=2&f=3qweqweqweqqwewewqwq
    2.3. 签名:利用 SHA1 签名得出的值作为 sign 参数

# 依赖
使用到了开源工具包: [hutool](https://www.hutool.cn/)
