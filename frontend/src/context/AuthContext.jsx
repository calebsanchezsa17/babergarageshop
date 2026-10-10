import React,{createContext,useContext,useEffect,useState} from 'react';import {api} from '../services/api';
const C=createContext(null);export const useAuth=()=>useContext(C);
export function AuthProvider({children}){const [user,setUser]=useState(null),[loading,setLoading]=useState(true);
 useEffect(()=>{const t=localStorage.getItem('bg_token');if(!t){setLoading(false);return}api('/api/auth/me').then(setUser).catch(()=>localStorage.removeItem('bg_token')).finally(()=>setLoading(false))},[]);
 async function login(usernameOrEmail,password){const r=await api('/api/auth/login',{method:'POST',body:{usernameOrEmail,password}});localStorage.setItem('bg_token',r.token);setUser(r.user);return r.user}
 function logout(){localStorage.removeItem('bg_token');setUser(null)}
 async function refresh(){const u=await api('/api/auth/me');setUser(u);return u}
 return <C.Provider value={{user,setUser,loading,login,logout,refresh}}>{children}</C.Provider>}
