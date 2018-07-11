var iw = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
var ih = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
var ix = (window.outerWidth - iw)/2.0;
var iy = window.outerHeight - ih;
var result = [[window.screenX+7.0001,window.screenY+0.0001,window.outerWidth-14.0001,window.outerHeight-7.0001],[ix+0.0001,iy+0.0001,iw+0.0001,ih+0.0001]];