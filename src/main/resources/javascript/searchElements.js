var parent = arguments[0], result = [];
const tag = arguments[1];
const attributes = arguments[2];
const attributesLen = arguments[3];	

if(parent == null){
	parent = window.document;
}

const elements = parent.getElementsByTagName(tag);

function addElement(e){
	let rec = e.getBoundingClientRect();
	result[result.length] = {'ats-elt':[e, e.tagName, rec.width+0.0001, rec.height+0.0001, rec.left+0.0001, rec.top+0.0001]};
	return result[result.length-1];
};

for(var i = 0, len = elements.length; i < len; i++){
	
	let e = elements[i];
	
	if(attributesLen == 0){
		addElement(e);
	}else{
	
		let attributesList = [];
		for(var j=0; j < attributesLen; j++){
		
			let attributeName = attributes[j];
						
			if(attributeName == 'text'){
				let textValue = e.textContent;
				if(textValue){
					attributesList.push(['text', textValue.trim().replace(/\xA0/g,' ').replace(/\s+/g,' ')]);
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
			let newElem = addElement(e);
			for(var k=0; k < attributesLen; k++){
				newElem[attributesList[k][0]] = attributesList[k][1];
			}
		}
	}
};