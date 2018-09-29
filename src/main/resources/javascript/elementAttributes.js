const element = arguments[0];
var attrib, textValue, result = {};

for(var i=0, len=element.attributes.length; i<len; i++){
	attrib = element.attributes[i];
	if(attrib.name != 'value'){
		result[attrib.name] = attrib.value;
	}
};

textValue = element.textContent;
if(textValue){
	result['text'] = textValue.trim().replace(/\xA0/g," ").replace(/\s+/g," ");
}

if(element.value){
	result['value'] = element.value;
	if(element.tagName == 'INPUT'){
		if(element.type == 'radio' || element.type == 'checkbox'){
			if(element.checked == true){
				result['checked'] = 'true';
			}else{
				result['checked'] = 'false';
			}			
		}
	}
};