webpackJsonp([9],{DnfT:function(t,e,i){"use strict";(function(t){var a=i("BO1k"),s=i.n(a);e.a={name:"Around",data:function(){return{blogs:null,likeFlag:null,headers:{headers:{token:window.localStorage.getItem("token")}}}},created:function(){var t=this;this.$http.get("http://43.139.180.101:8080/blogs/around",this.headers).then(function(e){t.blogs=e.data.data,console.log(t.blogs)})},methods:{createLikes:function(t){this.likeFlag=Number(this.blogs[t.target.getAttribute("index")].isLike),0===this.likeFlag?(this.likeFlag=1,this.blogs[t.target.getAttribute("index")].isLike=1,this.blogs[t.target.getAttribute("index")].love_user_number+=1):(this.likeFlag=0,this.blogs[t.target.getAttribute("index")].isLike=0,this.blogs[t.target.getAttribute("index")].love_user_number-=1);var e=new URLSearchParams;e.append("blogId",t.target.getAttribute("flag")),e.append("flag",this.likeFlag),this.$http.post("http://43.139.180.101:8080/likes/blog",e,this.headers).then(function(t){})},addFollow:function(t){var e=this;this.$http.post("http://43.139.180.101:8080/friends/"+t.target.getAttribute("flag"),{},this.headers).then(function(i){if(23001===i.data.code){e.$message({type:"success",message:"关注成功",duration:"700"});var a=!0,l=!1,o=void 0;try{for(var n,r=s()(document.querySelectorAll("button[flag='"+t.target.getAttribute("flag")+"']"));!(a=(n=r.next()).done);a=!0){var c=n.value,g=document.createElement("button"),d=document.createElement("i");g.style.position="absolute",g.style.left="460px",g.style.width="80px",g.style.border="2px lightgray solid",g.style.backgroundColor="white",g.style.color="lightgray",g.style.fontWeight="lighter",g.style.height="30px",g.style.borderRadius="15px",g.style.cursor="not-allowed",d.className="el-icon-check",4===c.innerText.length?(d.innerText=" 互相关注",g.style.width="100px"):d.innerText=" 已关注",g.appendChild(d),c.after(g),c.remove()}}catch(t){l=!0,o=t}finally{try{!a&&r.return&&r.return()}finally{if(l)throw o}}}})},tipLogin:function(){this.$message({duration:"1000",message:"请先进行登录",type:"error"})},toComment:function(t){window.location.href="http://43.139.180.101:8080/blogComment/"+t.target.id},changeImage:function(t){var e=t.target,i=e.height/e.width;t.target.width>e.height?(e.width=130,e.height=Math.floor(130*i),e.style.marginTop=(130-e.height)/2+"px",e.style.marginBottom=(130-e.height)/2+"px"):t.target.width<e.height?(e.height=130,e.width=Math.floor(130/i),e.style.marginLeft=(130-e.width)/2+"px",e.style.marginRight=(130-e.width)/2+"px"):(e.width=130,e.height=130,e.style.borderRadius="4px"),e.style.display="inline-block"},changeVideo:function(t){var e=t.target,i=e.clientHeight/e.clientWidth;e.clientWidth>e.clientHeight?(e.width=130,e.height=Math.floor(130*i),e.style.marginTop=(130-e.height)/2+"px",e.style.marginBottom=(130-e.height)/2+"px"):e.clientWidth<e.clientHeight?(e.height=130,e.width=Math.floor(130/i),e.style.marginLeft=(130-e.width)/2+"px",e.style.marginRight=(130-e.width)/2+"px"):(e.width=130,e.height=130,e.style.borderRadius="4px"),e.style.display="block"},enlargeImage:function(e){t("#imageModal").modal({backdrop:"static"});var i=t("#originalImage")[0];i.src=e.target.src,i.width=4*e.target.width,i.height=4*e.target.height}}}}).call(e,i("7t+N"))},I7qY:function(t,e,i){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a=i("DnfT"),s={render:function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("div",{staticStyle:{"margin-top":"68px"},attrs:{id:"main"}},[t._l(t.blogs,function(e,a){return i("div",{staticClass:"blogDiv"},[i("div",{staticClass:"ownerInfoDiv"},[i("a",{attrs:{href:e.user_url}},[i("img",{staticClass:"img-circle",attrs:{src:e.user_avatar,width:"50px",height:"50px",alt:"头像"}})]),t._v(" "),e.loginStatus&&e.isSelf?i("button",{staticStyle:{width:"40px",border:"none","background-color":"#ff8200",height:"20px","font-size":"8px",position:"relative",bottom:"10px","border-radius":"10px"}},[t._v("\n         本人\n      ")]):t._e(),t._v(" "),i("span",{staticClass:"nicknameSpan"},[t._v(t._s(e.user_nickname))]),t._v(" "),i("span",{staticClass:"createTimeSpan"},[t._v("发布于"+t._s(e.create_time))]),t._v(" "),e.loginStatus?t._e():i("button",{staticClass:"followButton",attrs:{title:"关注"},on:{click:t.tipLogin}},[i("i",{staticClass:"el-icon-plus",staticStyle:{"font-weight":"bolder"}}),t._v(" 关注\n      ")]),t._v(" "),e.loginStatus&&!e.is_self&&0===e.relation?i("button",{staticClass:"followButton",attrs:{title:"关注",flag:e.user_id},on:{click:t.addFollow}},[i("i",{staticClass:"el-icon-plus",staticStyle:{"font-weight":"bolder"},attrs:{flag:e.user_id}}),t._v(" 关注\n      ")]):t._e(),t._v(" "),e.loginStatus&&!e.is_self&&8===e.relation?i("button",{staticClass:"followButton",attrs:{title:"回关",flag:e.user_id},on:{click:t.addFollow}},[i("i",{staticClass:"el-icon-plus",staticStyle:{"font-weight":"bolder"},attrs:{flag:e.user_id}}),t._v(" 回关 \n      ")]):t._e(),t._v(" "),e.loginStatus&&!e.is_self&&4===e.relation?i("button",{staticClass:"attention"},[i("i",{staticClass:"el-icon-check"}),t._v(" 已关注\n      ")]):t._e(),t._v(" "),e.loginStatus&&!e.is_elf&&2===e.relation?i("button",{staticClass:"attention",staticStyle:{width:"100px"}},[i("i",{staticClass:"el-icon-check"}),t._v(" 互相关注\n      ")]):t._e()]),t._v(" "),i("div",{staticClass:"blogInfoDiv"},[i("div",{staticStyle:{"margin-left":"10px",width:"566px","word-break":"break-all","word-wrap":"break-word"}},[t._v(t._s(e.content)+"\n      ")]),t._v(" "),i("div",{staticStyle:{"margin-top":"15px",overflow:"hidden"}},[t._l(e.image,function(e){return"http://43.139.180.101:8080/static/upload/"!==e?i("div",{staticClass:"imageDiv"},[i("img",{staticClass:"image",staticStyle:{cursor:"zoom-in"},attrs:{src:e,title:"放大图像",alt:"图片"},on:{load:t.changeImage,dblclick:t.enlargeImage}})]):t._e()}),t._v(" "),t._l(e.video,function(e){return"http://43.139.180.101:8080/static/upload/"!==e?i("div",{staticClass:"imageDiv"},[i("video",{staticClass:"image",attrs:{src:e,controls:""},on:{canplay:t.changeVideo}})]):t._e()})],2)]),t._v(" "),i("div",{staticStyle:{"margin-left":"200px","margin-top":"25px"}},[i("span",{staticClass:"el-icon-chat-dot-square",staticStyle:{"margin-right":"150px",cursor:"pointer","font-size":"20px",color:"grey"},attrs:{title:"评论",id:e.id},on:{click:t.toComment}}),t._v(" "),e.loginStatus?i("span",{staticClass:"iconfont icon-like",class:{likeOrange:1==e.isLike},staticStyle:{cursor:"pointer","font-size":"25px"},attrs:{title:"点赞",flag:e.id,index:a},on:{click:t.createLikes}},[0!=e.love_user_number?i("span",{staticStyle:{"font-size":"14px","margin-left":"2px",position:"relative",bottom:"4px"},attrs:{index:a,flag:e.id}},[t._v(t._s(e.love_user_number))]):t._e()]):i("span",{staticClass:"iconfont icon-like",staticStyle:{cursor:"pointer","font-size":"25px",color:"grey"},on:{click:t.tipLogin}},[i("span",{staticStyle:{"font-size":"14px","margin-left":"2px",position:"relative",bottom:"4px"}},[t._v(t._s(e.love_user_number))])])])])}),t._v(" "),t.blogs&&0===t.blogs.length?i("div",{staticStyle:{"text-align":"center",color:"grey"}},[i("h5",[t._v("~~还没有人分享博客,快去添加您的博客吧!~~")])]):t._e(),t._v(" "),t._m(0)],2)},staticRenderFns:[function(){var t=this.$createElement,e=this._self._c||t;return e("div",{staticClass:"modal fade",attrs:{tabindex:"-1",role:"dialog","aria-labelledby":"myLargeModalLabel",id:"imageModal"}},[e("div",{staticClass:"modal-dialog modal-lg",attrs:{role:"document"}},[e("div",{staticClass:"modal-content"},[e("button",{staticClass:"close",attrs:{type:"button","data-dismiss":"modal","aria-label":"Close"}},[e("span",{staticStyle:{"font-size":"35px","margin-right":"10px"},attrs:{"aria-hidden":"true"}},[this._v("×")])]),this._v(" "),e("img",{attrs:{src:"",alt:"图像",id:"originalImage"}})])])])}]};var l=function(t){i("dl2f")},o=i("VU/8")(a.a,s,!1,l,"data-v-4065e7ca",null);e.default=o.exports},dl2f:function(t,e){}});
//# sourceMappingURL=9.6b7ca63d58b5ad1f67fc.js.map