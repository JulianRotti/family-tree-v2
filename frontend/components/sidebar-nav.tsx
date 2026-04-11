"use client"

import { motion } from "framer-motion"
import { TreePine, Map, BookOpen, ChevronLeft, ChevronRight } from "lucide-react"
import { useState } from "react"
import { cn } from "@/lib/utils"

interface SidebarNavProps {
  activeSection: 'tree' | 'map' | 'archive'
  onNavigate: (section: 'tree' | 'map' | 'archive') => void
  onBackToLanding: () => void
}

const navItems = [
  { id: 'tree' as const, label: 'Family Tree', icon: TreePine },
  { id: 'map' as const, label: 'Time Map', icon: Map },
  { id: 'archive' as const, label: 'Archive', icon: BookOpen },
]

export function SidebarNav({ activeSection, onNavigate, onBackToLanding }: SidebarNavProps) {
  const [expanded, setExpanded] = useState(false)

  return (
    <motion.aside
      initial={{ x: -100, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      className={cn(
        "fixed left-0 top-0 h-full bg-card/80 backdrop-blur-sm border-r border-border z-40 transition-all duration-300",
        expanded ? "w-56" : "w-20"
      )}
    >
      <div className="flex flex-col h-full py-6">
        {/* Logo / Back Button */}
        <button
          onClick={onBackToLanding}
          className="flex items-center gap-3 px-6 mb-8 text-foreground hover:text-terracotta transition-colors"
        >
          <ChevronLeft className="w-5 h-5 shrink-0" />
          {expanded && (
            <motion.span
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="text-sm font-medium truncate"
            >
              Back Home
            </motion.span>
          )}
        </button>

        {/* Nav Items */}
        <nav className="flex-1 px-3">
          <ul className="space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon
              const isActive = activeSection === item.id
              return (
                <li key={item.id}>
                  <button
                    onClick={() => onNavigate(item.id)}
                    className={cn(
                      "w-full flex items-center gap-3 px-3 py-3 rounded-lg transition-all duration-200",
                      isActive
                        ? "bg-terracotta/10 text-foreground"
                        : "text-muted-foreground hover:bg-secondary hover:text-foreground"
                    )}
                  >
                    <Icon
                      className={cn(
                        "w-5 h-5 shrink-0 transition-colors",
                        isActive && "text-terracotta"
                      )}
                    />
                    {expanded && (
                      <motion.span
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        className="text-sm truncate"
                      >
                        {item.label}
                      </motion.span>
                    )}
                  </button>
                </li>
              )
            })}
          </ul>
        </nav>

        {/* Expand Toggle */}
        <button
          onClick={() => setExpanded(!expanded)}
          className="mx-3 flex items-center justify-center gap-2 px-3 py-2 text-muted-foreground hover:text-foreground transition-colors rounded-lg hover:bg-secondary"
        >
          {expanded ? (
            <>
              <ChevronLeft className="w-4 h-4" />
              <span className="text-xs">Collapse</span>
            </>
          ) : (
            <ChevronRight className="w-4 h-4" />
          )}
        </button>
      </div>
    </motion.aside>
  )
}
