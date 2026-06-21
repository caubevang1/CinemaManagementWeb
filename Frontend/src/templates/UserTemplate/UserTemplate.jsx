import React from 'react'
import { Outlet } from 'react-router-dom'
import Footer from './Footer'
import Header from './Header'
import ChatWidget from '../../components/Chat/ChatWidget'

export default function UserTemplate() {
  return (
    <>
        <Header />
        <Outlet />
        <Footer />
        <ChatWidget />
    </>
  )
}
