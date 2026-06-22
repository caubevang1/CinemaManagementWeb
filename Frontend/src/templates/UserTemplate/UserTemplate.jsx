import React from 'react'
import { Outlet } from 'react-router-dom'
import Footer from './Footer'
import Header from './Header'
import ChatSocketManager from '../../components/Chat/ChatSocketManager'

export default function UserTemplate() {
  return (
    <>
        <Header />
        <Outlet />
        <Footer />
        <ChatSocketManager />
    </>
  )
}
