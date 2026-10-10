import React,{createContext,useContext,useEffect,useState} from 'react';import {API} from '../services/api';
const C=createContext(null);export const useBrand=()=>useContext(C);
export function BrandProvider({children}){const [brand,setBrand]=useState({businessName:'BARBER GARAGE',accentColor:'#c99545'});const load=()=>fetch(API+'/api/public/branding').then(r=>r.json()).then(setBrand).catch(()=>{});useEffect(()=>{load()},[]);return <C.Provider value={{brand,setBrand,load}}>{children}</C.Provider>}
