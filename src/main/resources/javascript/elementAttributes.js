const element = arguments[0];
var attrib, textValue, result = [];

function addValue(k, v){
	result[result.length] = [k, v];
}

for(var i=0, len=element.attributes.length; i<len; i++){
	attrib = element.attributes[i];
	if(attrib.name != 'value'){
		addValue(attrib.name, attrib.value);
	}
};

textValue = element.innerText || element.textContent;
if(textValue){
	addValue('text', textValue.trim().replace(/\xA0/g," ").replace(/\s+/g," "));
}

if(element.value){
	addValue('value', element.value);
	if(element.tagName == 'INPUT'){
		if(element.type == 'radio' || element.type == 'checkbox'){
			if(element.checked == true){
				addValue('checked', 'true');
			}else{
				addValue('checked', 'false');
			}			
		}
	}
};