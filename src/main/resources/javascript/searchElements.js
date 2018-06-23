var parent = arguments[0];
const tag = arguments[1];
const attributes = arguments[2];
const attributesLen = arguments[3];	

if(parent == null){
	parent = window.document;
}

const elements = parent.querySelectorAll(tag);
var attributesList, attributeName, textValue, foundElement, e, result = [];

for(var i = 0, len = elements.length; i < len; i++){
	e = elements[i];
	
	if(attributesLen == 0){
		result.push(getElement(e));
	}else{
	
		attributesList = [];
		for(var j=0; j < attributesLen; j++){
		
			attributeName = attributes[j];
						
			if(attributeName == 'text'){
				textValue = e.textContent;
				if(textValue){
					textValue = textValue.trim();
					textValue = textValue.replace(/\xA0/g,' ');
					textValue = textValue.replace(/\s+/g,' ');
					attributesList.push([attributeName, textValue]);
				}
			}else{
				if(attributeName == 'checked' && e.tagName == 'INPUT' && (e.type == 'radio' || e.type == 'checkbox')){
					if(e.checked == true){
						attributesList.push(['checked', 'true']);
					}else{
						attributesList.push(['checked', 'false']);
					}		
				}else{
					if(e.hasAttribute(attributeName)){
						attributesList.push([attributeName, e.getAttribute(attributeName)]);
					}else{
						attributesList.push([attributeName, window.getComputedStyle(e,null).getPropertyValue(attributeName)]);
					}
				}
			}
		}
	
		if(attributesList.length == attributesLen){
			foundElement = getElement(e);
			for(var k=0; k < attributesLen; k++){
				foundElement[attributesList[k][0]] = attributesList[k][1];
			}
			result.push(foundElement);
		}
	}
};

function getElement(el){
	var rec = el.getBoundingClientRect();
	return {'ats-elt':{index:result.length, tag:el.tagName.toLowerCase(), value:el, x:rec.left+0.00001, y:rec.top+0.00001, width:rec.width+0.00001, height:rec.height+0.00001}};
};