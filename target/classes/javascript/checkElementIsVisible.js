var element = arguments[0];
var rect = element.getBoundingClientRect();
var result = rect.bottom > 0 &&	rect.right > 0 && rect.left < (window.innerWidth || document.documentElement.clientWidth) && rect.top < (window.innerHeight || document.documentElement.clientHeight);