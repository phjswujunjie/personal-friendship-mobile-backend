webpackJsonp([7],{Qnjl:function(e,t,s){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=s("t6J1"),i={render:function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("div",{staticClass:"main",staticStyle:{"margin-left":"10px"}},[s("div",{attrs:{id:"background"}},[s("div",[s("div",[s("h4",{staticStyle:{"border-bottom":"1px lightgray solid","padding-bottom":"10px","margin-bottom":"0"}},[e._v(e._s(e.toNickname))])]),e._v(" "),s("div",[s("ul",{staticStyle:{overflow:"auto"},attrs:{id:"messageUl"}},e._l(e.chatMessages,function(t){return s("li",{key:t.id,class:{messageRight:t.user_id==e.$store.state.selfId}},[s("img",{staticClass:"img-circle",staticStyle:{cursor:"pointer"},attrs:{src:t.avatar,width:"40px",height:"40px"}}),e._v(" "),s("el-card",{staticClass:"box-card",class:{messageDivRight:t.user_id==e.$store.state.selfId,messageDivLeft:t.user_id!=e.$store.state.selfId},attrs:{"body-style":{padding:"8px"}}},[s("div",{staticClass:"text item",domProps:{innerHTML:e._s(t.content)}})])],1)}),0)])]),e._v(" "),s("div",{staticStyle:{"border-top":"1px lightgray solid"}},[e._m(0),e._v(" "),s("textarea",{directives:[{name:"model",rawName:"v-model",value:e.message,expression:"message"}],staticStyle:{width:"90%",height:"80px",resize:"unset","background-color":"#f0f1f4",border:"none",outline:"none"},attrs:{id:"text",placeholder:"在这里输入信息"},domProps:{value:e.message},on:{input:function(t){t.target.composing||(e.message=t.target.value)}}}),e._v(" "),s("button",{class:{disableSend:""===e.message.trim()},attrs:{id:"sendButton"},on:{click:e.sendMessage}},[e._v("发送")])])])])},staticRenderFns:[function(){var e=this.$createElement,t=this._self._c||e;return t("div",[t("i",{staticClass:"glyphicon glyphicon-picture",staticStyle:{"font-size":"15px"}}),this._v(" "),t("i",{staticClass:"glyphicon glyphicon-picture",staticStyle:{"margin-left":"10px","font-size":"15px"}}),this._v(" "),t("i",{staticClass:"glyphicon glyphicon-picture",staticStyle:{"margin-left":"10px","font-size":"15px"}}),this._v(" "),t("i",{staticClass:"glyphicon glyphicon-picture",staticStyle:{"margin-left":"10px","font-size":"15px"}})])}]};var o=function(e){s("o2+l")},n=s("VU/8")(a.a,i,!1,o,null,null);t.default=n.exports},"o2+l":function(e,t){},t6J1:function(e,t,s){"use strict";(function(e){var a=s("mvHQ"),i=s.n(a);s("nNlw");t.a={name:"User",data:function(){return{headers:{headers:{token:window.localStorage.getItem("token")}},toNickname:null,chatMessages:null,selfAvatar:null,messageSocket:new WebSocket("ws://43.139.180.101:8080/chatWebSocket"),message:""}},mounted:function(){var t=this;console.log("mounted......"),document.getElementById("text").focus(),this.messageSocket.onmessage=function(s){var a=JSON.parse(s.data);if(a.fromId==t.$route.params.id)e("<li class='messageLeft'><img class='img-circle' width='40px' height='40px' style='cursor: pointer' src="+a.fromUserAvatar+"><div class='el-card box-card is-always-shadow messageDivLeft'><div class=\"el-card__body\" style='padding: 8px'><div class=\"text item\">"+a.content+"</div></div></div></li>").appendTo(e("#messageUl"));else{var i=new URLSearchParams;i.append("content",a.content),i.append("createTime",a.createTime),i.append("fromUserAvatar",a.fromUserAvatar),i.append("fromId",a.fromId),console.log(i),t.$http.post("http://43.139.180.101:8080/chatMessages/storageUnreadMessage",i,t.headers).then(function(e){console.log(e.data)})}document.getElementById("messageUl").scrollTop=document.getElementById("messageUl").scrollHeight}},created:function(){var e=this;this.$http.get("http://43.139.180.101:8080/chatMessages/"+this.$route.params.id,this.headers).then(function(t){20001===t.data.code?(e.selfAvatar=t.data.data[0].selfAvatar,e.toNickname=t.data.data[0].toNickname,e.chatMessages=t.data.data.slice(1,t.data.data.length),e.$nextTick(function(){document.getElementById("messageUl").scrollTop=document.getElementById("messageUl").scrollHeight})):location.href="http://43.139.180.101:8080/homepage"}),this.messageSocket.onopen=function(){var t={user_id:e.$store.state.selfId,to_id:e.$route.params.id};e.messageSocket.send(i()(t))}},methods:{sendMessage:function(t){if(""!==this.message.trim()){this.message=this.message.replace(/\r\n/g,"<br/>").replace(/\n/g,"<br/>"),e("<li class='messageRight'><img class='img-circle' width='40px' height='40px' style='cursor: pointer' src="+this.selfAvatar+"><div class='el-card box-card is-always-shadow messageDivRight'><div class=\"el-card__body\" style='padding: 8px'><div class=\"text item\">"+this.message+"</div></div></div></li>").appendTo(e("#messageUl")),document.getElementById("messageUl").scrollTop=document.getElementById("messageUl").scrollHeight;var s={content:this.message,user_id:this.$store.state.selfId,to_id:this.$route.params.id};this.messageSocket.send(i()(s)),this.message="",document.getElementById("text").focus()}}},beforeDestroy:function(){this.messageSocket.close()}}}).call(t,s("7t+N"))}});
//# sourceMappingURL=7.5d8710db490209798fab.js.map