var element = arguments[0];
var result = [];
var o = getComputedStyle(element);
for(var i = 0; i < o.length; i++){
	result.push([o[i], o.getPropertyValue(o[i])]);
};