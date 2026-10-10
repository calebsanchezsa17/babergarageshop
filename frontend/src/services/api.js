export const API=(import.meta.env.VITE_API_URL||'http://localhost:8093').replace(/\/$/,'');
export const AI=(import.meta.env.VITE_AI_URL||'http://localhost:8000').replace(/\/$/,'');
export function token(){return localStorage.getItem('bg_token')||''}
export async function api(path,{method='GET',body,headers={}}={}){
 const r=await fetch(API+path,{method,headers:{'Content-Type':'application/json',...(token()?{Authorization:`Bearer ${token()}`}:{}) ,...headers},body:body===undefined?undefined:JSON.stringify(body)});
 const text=await r.text(); let data={}; try{data=text?JSON.parse(text):{}}catch{data={message:text}}
 if(!r.ok) throw new Error(data.message||`Error ${r.status}`); return data;
}
export const fileToDataUrl=file=>new Promise((resolve,reject)=>{const r=new FileReader();r.onload=()=>resolve(r.result);r.onerror=reject;r.readAsDataURL(file)});
