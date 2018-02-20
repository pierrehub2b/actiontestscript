var result = [];
addIframe(window.document);

function addIframe(doc){
	var iframes = doc.querySelectorAll('iframe');
	for(var i = 0, len = iframes.length; i < len; i++){
		var ifrm = iframes[i];
		ifrm = (ifrm.contentWindow) ? ifrm.contentWindow : (ifrm.contentDocument.document) ? ifrm.contentDocument.document : ifrm.contentDocument;
		addIframe(ifrm.document);
	}
	result.push(doc.readyState=='complete');
}