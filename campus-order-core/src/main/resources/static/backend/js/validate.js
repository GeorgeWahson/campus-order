function isValidUsername(str) {
    return ['admin', 'editor'].indexOf(str.trim()) >= 0;
}

function isExternal(path) {
    return /^(https?:|mailto:|tel:)/.test(path);
}

function isCellPhone(val) {
    if (!/^1(3|4|5|6|7|8)\d{9}$/.test(val)) {
        return false
    } else {
        return true
    }
}

//校验账号
function checkUserName(rule, value, callback) {
    if (value == "") {
        callback(new Error("请输入账号"))
    } else if (value.length > 20 || value.length < 3) {
        callback(new Error("账号长度应是3-20"))
    } else {
        callback()
    }
}

//校验姓名
function checkName(rule, value, callback) {
    if (value == "") {
        callback(new Error("请输入姓名"))
    } else if (value.length > 12) {
        callback(new Error("账号长度应是1-12"))
    } else {
        callback()
    }
}

function checkPhone(rule, value, callback) {
    // let phoneReg = /(^1[3|4|5|6|7|8|9]\d{9}$)|(^09\d{8}$)/;
    if (value == "") {
        callback(new Error("请输入手机号"))
    } else if (!isCellPhone(value)) {//引入methods中封装的检查手机格式的方法
        callback(new Error("请输入正确的手机号!"))
    } else {
        callback()
    }
}


function validID(rule, value, callback) {
    // 身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X
    let reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    if (value == '') {
        callback(new Error('请输入身份证号码'))
    } else if (reg.test(value)) {
        callback()
    } else {
        callback(new Error('身份证号码不正确'))
    }
}

/*
  这个正则表达式 ^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$ 的含义如下：
  (?=.*[0-9])：至少有一个数字。
  (?=.*[a-zA-Z])：至少有一个字母（无论大小写）。
  (?=.*[@#$%^&+=])：至少有一个特殊字符。
  (?=\\S+$)：没有连续的空白字符。
  .{8,}：长度至少为8个字符。
*/
var password;

function validatePassword(rule, value, callback) {
    // 定义正则表达式以匹配规则
    // let regex = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=_])(?=\\S+$).{8,}$/;
    // let regex = /^(?=.*\d)(?=.*[a-zA-Z])(?=.*[^\da-zA-Z\s]).{1,9}$/;
    let regex = /^(?=.*\d)(?=.*[a-zA-Z])(?=.*[@$!%*_?&])[A-Za-z\d@$!%*_?&]{8,}$/;
    password = value;
    // let regex= /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@#$%^&*_+<>:])[A-Za-z\d@#$%^&*_+<>:]{8,}$/;
    // console.log(value)
    // console.log(regex.test(value))
    if (regex.test(value)) {

        callback()
    } else {
        if (value.length < 8) {
            callback(new Error('密码长度至少8位'))
        } else {
            callback(new Error('至少包含一个数字，字母及\'@$!%*_?&\'特殊字符'))
        }
    }
}

function validateRePassword(rule, value, callback) {
    if (value === password) {
        callback()
    } else {
        callback(new Error("两次密码不一致!"))
    }

}

