var parent = arguments[0];
const tag = arguments[1];
const attributes = arguments[2];
const attributesLen = arguments[3];	

if(parent == null){
	parent = window.document;
};

const elts = parent.getElementsByTagName(tag);
const eltsLength = elts.length;

var result = [], addElement = function (e){
	let rec = e.getBoundingClientRect();
	result[result.length] = {'ats-elt':[e, e.tagName, rec.width+0.0001, rec.height+0.0001, rec.left+0.0001, rec.top+0.0001]};
	return result[result.length-1];
};

if(attributesLen == 0){

	for(var h = 0; h < eltsLength; h++){
		addElement(elts[h]);
	}

}else{

	var i = 0, loop = function (){

		let e = elts[i], attributesList = [];

		for(var j = 0; j < attributesLen; j++){

			let attributeName = attributes[j];

			if(attributeName == 'text'){
				let textValue = e.textContent;
				if(textValue){
					attributesList[attributesList.length] = ['text', textValue.replace(/\xA0/g,' ').trim()];
				}
			}else{
				if(attributeName == 'checked' && e.tagName == 'INPUT' && (e.type == 'radio' || e.type == 'checkbox')){
					if(e.checked == true){
						attributesList[attributesList.length] = ['checked', 'true'];
					}else{
						attributesList[attributesList.length] = ['checked', 'false'];
					}		
				}else{
					if(e.hasAttribute(attributeName)){
						attributesList[attributesList.length] = [attributeName, e.getAttribute(attributeName)];
					}else{
						attributesList[attributesList.length] = [attributeName, window.getComputedStyle(e,null).getPropertyValue(attributeName)];
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

		i++;
	};
	for (; i < eltsLength; ) loop();
};