webpackJsonp([1],{"1alW":function(t,e,o){var r=o("kM2E");r(r.S,"Number",{isInteger:o("AKgy")})},AKgy:function(t,e,o){var r=o("EqjI"),i=Math.floor;t.exports=function(t){return!r(t)&&isFinite(t)&&i(t)===t}},"RRo+":function(t,e,o){t.exports={default:o("c45H"),__esModule:!0}},bNzW:function(t,e,o){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var r=o("RRo+"),i=o.n(r);function s(t){if(this.options={id:"",canvasId:"verifyCanvas",width:"100",height:"30",type:"blend",code:""},"[object Object]"==Object.prototype.toString.call(t))for(var e in t)this.options[e]=t[e];else this.options.id=t;this.options.numArr="0,1,2,3,4,5,6,7,8,9".split(","),this.options.letterArr="a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(","),this._init(),this.refresh()}function n(t,e){return Math.floor(Math.random()*(e-t)+t)}function a(t,e){return"rgb("+n(t,e)+","+n(t,e)+","+n(t,e)+")"}s.prototype={version:"1.0.0",_init:function(){var t=document.getElementById(this.options.id),e=document.createElement("canvas");this.options.width=t.offsetWidth>0?t.offsetWidth:"100",this.options.height=t.offsetHeight>0?t.offsetHeight:"30",e.id=this.options.canvasId,e.width=this.options.width,e.height=this.options.height,e.style.cursor="pointer",e.innerHTML="您的浏览器版本不支持canvas",t.appendChild(e);var o=this;e.onclick=function(){o.refresh()}},refresh:function(){this.options.code="";var t=document.getElementById(this.options.canvasId);if(t.getContext){var e=t.getContext("2d");if(e.textBaseline="middle",e.fillStyle=a(180,240),e.fillRect(0,0,this.options.width,this.options.height),"blend"==this.options.type)var o=this.options.numArr.concat(this.options.letterArr);else if("number"==this.options.type)o=this.options.numArr;else o=this.options.letterArr;for(var r=1;r<=4;r++){var i=o[n(0,o.length)];this.options.code+=i,e.font=n(this.options.height/2,this.options.height)+"px SimHei",e.fillStyle=a(50,160),e.shadowOffsetX=n(-3,3),e.shadowOffsetY=n(-3,3),e.shadowBlur=n(-3,3),e.shadowColor="rgba(0, 0, 0, 0.3)";var s=this.options.width/5*r,l=this.options.height/2,c=n(-30,30);e.translate(s,l),e.rotate(c*Math.PI/180),e.fillText(i,0,0),e.rotate(-c*Math.PI/180),e.translate(-s,-l)}for(r=0;r<4;r++)e.strokeStyle=a(40,180),e.beginPath(),e.moveTo(n(0,this.options.width),n(0,this.options.height)),e.lineTo(n(0,this.options.width),n(0,this.options.height)),e.stroke();for(r=0;r<this.options.width/4;r++)e.fillStyle=a(0,255),e.beginPath(),e.arc(n(0,this.options.width),n(0,this.options.height),1,0,2*Math.PI),e.fill()}},validate:function(t){return(t=t.toLowerCase())==this.options.code.toLowerCase()}},window.GVerify=s;var l={name:"Register",mounted:function(){this.verifyCode=new s({id:"verify-img",type:"blend"})},data:function(){var t=this;return{testCode:"",verifyCode:null,ruleForm:{account:"",pass:"",checkPass:""},rules:{pass:[{validator:function(e,o,r){""===o?r(new Error("请输入密码")):/^[a-zA-Z\d^*!_]{8,15}$/.test(o)?(""!==t.ruleForm.checkPass&&t.$refs.ruleForm.validateField("checkPass"),r()):r(new Error("密码必须以8-15位由字母,数字,常用字符组成"))},trigger:"blur"}],checkPass:[{validator:function(e,o,r){""===o?r(new Error("请再次输入密码")):o!==t.ruleForm.pass?r(new Error("两次输入密码不一致!")):r()},trigger:"blur"}],account:[{validator:function(e,o,r){if(!o)return r(new Error("账号不能为空"));var s=/^[1-9]\d{9}$/;setTimeout(function(){if(i()(o))if(s.test(o.toString())){var e=new URLSearchParams;e.append("account",t.ruleForm.account),t.$http.post("http://43.139.180.101:8080/accountIsExists",e).then(function(t){4e4===t.data.code?r(new Error("账号已经存在,请重新输入")):r()})}else r(new Error("账号长度必须为10的数字位且不能以0开头"));else r(new Error("请输入数字值"))},1e3)},trigger:"blur"}],testCode:[{validator:function(e,o,r){t.verifyCode.validate(t.testCode)?r():r(new Error("验证码错误"))},trigger:"blur"}]}}},methods:{submitForm:function(t){var e=this;this.$refs[t].validate(function(t){if(!t)return alert("请正确的填写数据的格式"),e.verifyCode.refresh(),!1;var o=new URLSearchParams;o.append("account",e.ruleForm.account),o.append("password",e.ruleForm.pass),e.$http.post("http://43.139.180.101:8080/registerUser",o).then(function(t){if(23001!==t.data.code)return alert("请输入正确的数据格式"),e.verifyCode.refresh(),!1;window.localStorage.setItem("token",t.data.data.token),window.location.href="http://43.139.180.101:8080/homepage"})})}}},c={render:function(){var t=this,e=t.$createElement,o=t._self._c||e;return o("div",{attrs:{id:"register"}},[o("h2",{staticStyle:{"text-align":"center","margin-bottom":"30px"}},[t._v("注册")]),t._v(" "),o("el-form",{ref:"ruleForm",staticClass:"demo-ruleForm",attrs:{model:t.ruleForm,"status-icon":"",rules:t.rules,"label-width":"100px"}},[o("el-form-item",{attrs:{label:"账号",prop:"account"}},[o("el-input",{attrs:{placeholder:"请输入由10位数字组成的账号"},model:{value:t.ruleForm.account,callback:function(e){t.$set(t.ruleForm,"account",t._n(e))},expression:"ruleForm.account"}})],1),t._v(" "),o("el-form-item",{attrs:{label:"密码",prop:"pass"}},[o("el-input",{attrs:{type:"password",autocomplete:"off",placeholder:"请输入8-15位由数字,字母,常用字符组成的密码"},model:{value:t.ruleForm.pass,callback:function(e){t.$set(t.ruleForm,"pass",e)},expression:"ruleForm.pass"}})],1),t._v(" "),o("el-form-item",{attrs:{label:"确认密码",prop:"checkPass"}},[o("el-input",{attrs:{type:"password",autocomplete:"off",placeholder:"请再次输入密码"},model:{value:t.ruleForm.checkPass,callback:function(e){t.$set(t.ruleForm,"checkPass",e)},expression:"ruleForm.checkPass"}})],1),t._v(" "),o("el-form-item",{attrs:{label:"验证码",prop:"testCode"}},[o("el-input",{attrs:{type:"text",autocomplete:"off",placeholder:"请输入下图中的验证码"},model:{value:t.testCode,callback:function(e){t.testCode=e},expression:"testCode"}})],1)],1),t._v(" "),o("div",{staticStyle:{width:"200px",height:"50px","margin-left":"160px",display:"inline-block"},attrs:{id:"verify-img"}}),t._v(" "),o("i",{staticStyle:{"font-size":"5px",color:"darkgray",position:"relative",bottom:"18px"}},[t._v("看不清?点击图片试试")]),t._v(" "),t._m(0),t._v(" "),o("button",{attrs:{id:"submitRegister"},on:{click:function(e){return t.submitForm("ruleForm")}}},[t._v("注册")])],1)},staticRenderFns:[function(){var t=this.$createElement,e=this._self._c||t;return e("h2",{staticStyle:{"text-align":"right",color:"gray","font-size":"10px"}},[this._v("已有账号?"),e("a",{staticStyle:{cursor:"pointer"},attrs:{href:"http://43.139.180.101:8080/login"}},[this._v("立即登录!")])])}]};var h=o("VU/8")(l,c,!1,function(t){o("yFBX")},"data-v-2055ba96",null);e.default=h.exports},c45H:function(t,e,o){o("1alW"),t.exports=o("FeBl").Number.isInteger},yFBX:function(t,e){}});
//# sourceMappingURL=1.c4d1d9a4463bf20deceb.js.map