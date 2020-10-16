var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
  coll[i].firstElementChild.innerText = "+ " + coll[i].firstElementChild.innerText.split('.')[coll[i].firstElementChild.innerText.split('.').length-1];
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");
    var content = this.nextElementSibling;
    if (content.style.maxHeight){
      content.style.maxHeight = null;
	  this.firstElementChild.innerText = this.firstElementChild.innerText.replace('-', '+')
    } else {
      content.style.maxHeight = content.scrollHeight + "px";
	  this.firstElementChild.innerText = this.firstElementChild.innerText.replace('+', '-')
    } 
  });
}