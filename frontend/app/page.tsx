"use client"

import { useState } from "react"
import { AnimatePresence, motion } from "framer-motion"
import { LandingCanvas } from "@/components/landing-canvas"
import { SidebarNav } from "@/components/sidebar-nav"
import { TreeView } from "@/components/tree-view"
import { MapView } from "@/components/map-view"
import { ArchiveView } from "@/components/archive-view"

type Section = 'landing' | 'tree' | 'map' | 'archive'

export default function Home() {
  const [activeSection, setActiveSection] = useState<Section>('landing')

  const handleNavigate = (section: 'tree' | 'map' | 'archive') => {
    setActiveSection(section)
  }

  const handleBackToLanding = () => {
    setActiveSection('landing')
  }

  return (
    <main className="min-h-screen bg-background">
      <AnimatePresence mode="wait">
        {activeSection === 'landing' ? (
          <motion.div
            key="landing"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            transition={{ duration: 0.4 }}
          >
            <LandingCanvas onNavigate={handleNavigate} />
          </motion.div>
        ) : (
          <motion.div
            key="app"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.4 }}
            className="flex min-h-screen"
          >
            <SidebarNav
              activeSection={activeSection as 'tree' | 'map' | 'archive'}
              onNavigate={handleNavigate}
              onBackToLanding={handleBackToLanding}
            />
            
            <div className="flex-1 ml-20">
              <AnimatePresence mode="wait">
                {activeSection === 'tree' && (
                  <motion.div
                    key="tree"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.3 }}
                    className="h-screen"
                  >
                    <TreeView />
                  </motion.div>
                )}
                {activeSection === 'map' && (
                  <motion.div
                    key="map"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.3 }}
                    className="h-screen"
                  >
                    <MapView />
                  </motion.div>
                )}
                {activeSection === 'archive' && (
                  <motion.div
                    key="archive"
                    initial={{ opacity: 0, x: 20 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -20 }}
                    transition={{ duration: 0.3 }}
                    className="h-screen"
                  >
                    <ArchiveView />
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </main>
  )
}
