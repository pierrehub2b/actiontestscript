var element = arguments[0];
var scrolled = false;
var result = [];

function scroll(e){
	e.scrollIntoView(false);
	var rect = e.getBoundingClientRect();
	return [rect.right+0.00001, rect.top+0.00001];
};

var topOfPage = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop;
var heightOfPage = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
var elY = 0;
var elH = 0;

var leftOfPage = window.pageXOffset || document.documentElement.scrollLeft || document.body.scrollLeft;
var widthOfPage = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
var elX = 0;
var elW = 0;

if (document.layers){
	elY = element.y;
	elH = element.height;
	elX = element.x;
	elW = element.width;
}else{
	for(var p=element; p&&p.tagName!='BODY'; p=p.offsetParent){
		elY += p.offsetTop;
		elX += p.offsetLeft;
	}
	elH = element.offsetHeight;
	elW = element.offsetWidth;
}

if (((topOfPage + heightOfPage) < (elY + elH)) || (elY < topOfPage) || ((leftOfPage + widthOfPage) < (elX + elW)) || (elX < leftOfPage)) {
	result = scroll(element);
}