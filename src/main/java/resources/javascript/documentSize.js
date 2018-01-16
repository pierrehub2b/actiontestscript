var innerWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
var innerHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;

var innerX = (window.outerWidth - innerWidth)/2.0;
var innerY = window.outerHeight - innerHeight;

var result = {main:[window.screenX+7.00001, window.screenY+0.00001, window.outerWidth-14.00001, window.outerHeight-7.00001], sub:[innerX+0.00001, innerY+0.00001, innerWidth+0.00001, innerHeight+0.00001]};