var result = [], parent = arguments[0];
const tag = arguments[1];
const attributes = arguments[2];
const attributesLen = arguments[3];	

const screenOffsetX = (window.outerWidth - window.innerWidth)/2 + window.screenX + 0.0001;
const screenOffsetY = window.outerHeight - window.innerHeight + window.screenY + 0.0001;

if(parent == null){
	parent = window.document;
};

const elts = parent.getElementsByTagName(tag);
const eltsLength = elts.length;

var addElement = function (e, a){
	let rec = e.getBoundingClientRect();
	result[result.length] = [e, e.tagName, e.getAttribute('inputmode')=='numeric', rec.width+0.0001, rec.height+0.0001, rec.left+0.0001, rec.top+0.0001, screenOffsetX, screenOffsetY, a];
};

if(attributesLen == 0){

	for(var h = 0; h < eltsLength; h++){
		addElement(elts[h], {});
	}

}else{

	var i = 0, loop = function (){

		let e = elts[i], a = {}, j = 0;

		while(j < attributesLen){

			let attributeName = attributes[j];

			if(attributeName == 'text'){
				let textValue = e.textContent;
				if(textValue){
					a['text'] = textValue.replace(/\xA0/g,' ').trim();
				}
			}else{
				if(attributeName == 'checked' && e.tagName == 'INPUT' && (e.type == 'radio' || e.type == 'checkbox')){
					if(e.checked == true){
						a['checked'] = 'true';
					}else{
						a['checked'] = 'false';
					}		
				}else{
					if(e.hasAttribute(attributeName)){
						a[attributeName] = e.getAttribute(attributeName);
					}else{
						a[attributeName] = window.getComputedStyle(e,null).getPropertyValue(attributeName);
					}
				}
			}
			j++;
		}

		if(Object.keys(a).length == attributesLen){
			addElement(e, a);
		}
	};
	
	while (i < eltsLength) {
		loop();
		i++;
	}
};