var parent = arguments[0];
var tag = arguments[1];
var attributes = arguments[2];
var attributesLen = attributes.length;	

var result = [];

if(parent == null){
	parent = window.document;
}

var elements = parent.querySelectorAll(tag);
var attributesList, attributeName, textValue, idx, attributeData, e;

for(var i = 0, len = elements.length; i < len; i++){
	e = elements[i];
	
	if(attributesLen == 0){
		addElement(e);
	}else{
	
		attributesList = [];
		for(var j=0; j < attributesLen; j++){
		
			attributeName = attributes[j];
						
			if(attributeName == "text"){
				textValue = e.textContent;
				if(textValue){
					textValue = textValue.trim();
					textValue = textValue.replace(/\xA0/g," ");
					textValue = textValue.replace(/\s+/g," ");
					attributesList.push([attributeName, textValue]);
				}
			}else if(e.hasAttribute(attributeName)){
				attributesList.push([attributeName, e.getAttribute(attributeName)]);
			}else{
				attributesList.push([attributeName, window.getComputedStyle(e,null).getPropertyValue(attributeName)]);
			}
		}
	
		if(attributesList.length == attributesLen){
			idx = addElement(e);
			for(var k=0; k < attributesList.length; k++){
				attributeData = attributesList[k];
				result[idx-1][attributeData[0]] = attributeData[1];
			}
		}
	}
};

function addElement(el){
	var rec = el.getBoundingClientRect();
	return result.push({atsElem:{index:i, tag:el.tagName.toLowerCase(), value:el, x:rec.left+0.00001, y:rec.top+0.00001, width:rec.width+0.00001, height:rec.height+0.00001}});
};